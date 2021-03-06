//package com.codetinkerhack.example
//
//import java.lang.System._
//
//import javax.sound.midi._
//import com.codetinkerhack.midi._
//
//
///**
//  * Created by Evgeniy on 31/01/2015.
//  */
//object Keytar extends App {
//
//  override def main(args: Array[String]) {
//
//    val mh = new MidiHandler()
//    val output = mh.getReceivers.get("loopback")
//    val inputNanoPad = mh.getTransmitters.get("nanoPAD2")
//    val inputNanoKey = mh.getTransmitters.get("nanoKEY2")
//
//    output.open()
//    inputNanoPad.open()
//    inputNanoKey.open()
//
//    val chordAnalyzer = new ChordReader()
//    val midiDelay = new MidiDelay()
//    val keytar = new Keytar()
//    val chordTransformer = new ChordModifier()
//    val midiOut = MidiNode(output.getReceiver)
//    val midiInNanoPad = MidiNode(inputNanoPad.getTransmitters.get(0))
//
//    val midiInNanoKey = MidiNode(inputNanoKey.getTransmitters.get(0))
//
//    chordTransformer.setBaseChord(new Chord("E min"))
//
//    val instrumentSelector = MidiNode((message: MidiMessage, timeStamp: Long) => {
//
//      val baseInstrument = IndexedSeq(26, 30, 5, 7)
//      val soloInstrument = IndexedSeq(24, 29, 10, 40)
//
//      import javax.sound.midi.ShortMessage._
//
//      message match {
//        case m: ShortMessage if (m.getCommand == NOTE_OFF || m.getCommand == NOTE_ON) => {
//          // println(s"Note: ${m.getData1} index: ${(m.getData1 - 36)/16} base: ${baseInstrument((m.getData1 - 36)/16)} solo: ${soloInstrument((m.getData1 - 36)/16)}")
//          var messageList = List[(ShortMessage, Long)]()
//
//          messageList = (new ShortMessage(PROGRAM_CHANGE, 1, baseInstrument((m.getData1 - 36) / 16), 0), 0l) :: messageList
//          messageList = (new ShortMessage(PROGRAM_CHANGE, 2, soloInstrument((m.getData1 - 36) / 16), 0), 0l) :: messageList
//          messageList = (new ShortMessage(m.getCommand, m.getChannel, (m.getData1 - 36) % 16, 0), 0l) :: messageList
//
//          messageList
//        }
//        case _ => List((message, timeStamp))
//      }
//    })
//
//    // Scalalika
//    midiInNanoPad.out(0).connect(instrumentSelector)
//
//    midiInNanoKey.out(0).connect(keytar.in(2))
//
//    instrumentSelector.out(0)
//      .connect(chordAnalyzer).out(0)
//      .connect(keytar).out(0)
//      .connect(chordTransformer).out(0)
//
//    instrumentSelector.out(1).connect(midiOut)
//    instrumentSelector.out(2).connect(midiOut)
//
//    keytar.out(1).connect(midiDelay).out(1).connect(chordTransformer).out(1).connect(midiOut)
//    keytar.out(2).connect(chordTransformer).out(2).connect(midiOut)
//  }
//}
//
//class Keytar() extends MidiNode {
//
//
//  //    Emin
//  //    4, 11, 16, 19, 23, 36
//  private val baseNote = 40
//
//  private val blackNotes = Array[Int](0, 7, 0 , 12, 0 , 0 , 21, 0 , 24, 0 )
//  private val whiteNotes = Array[Int](4, 0, 11, 0 , 16, 19, 0 , 23, 0 , 28)
//  private val scale = (blackNotes zip whiteNotes).map( x=> x._1 + x._2)
//
//  private var currentBaseNote: Option[ShortMessage] = None
//
//  private var timeLapsed: Long = 0;
//  private var notesOnCache = Set[Int]()
//
//
//  override def receive(message: MidiMessage, timeStamp: Long): Unit = {
//    import ShortMessage._
//
//    message match {
//
//
//      case message: ShortMessage if (message.getCommand == NOTE_ON && message.getChannel == 0) => {
//
//        val note = baseNote + scale(0) - 12
//        currentBaseNote = Some(new ShortMessage(NOTE_ON, 1, note, 64))
//        send(new ShortMessage(PITCH_BEND, 1, 0, 0), 0)
//        send(currentBaseNote.get, 60)
//      }
//
//      case message: ShortMessage if (message.getCommand == NOTE_OFF && message.getChannel == 0) => {
//        currentBaseNote foreach (n => send(new ShortMessage(NOTE_OFF, 1, n.getMessage()(1), 0), 0))
//        notesOnCache.seq foreach (n => send(new ShortMessage(NOTE_OFF, 2, n, 0), 0))
//        notesOnCache = Set.empty
//      }
//
//      case message: ShortMessage if (message.getCommand == NOTE_ON && message.getChannel == 2) => {
//        val ccy = message.getData1
//
//       // println("Control change y: " + ccy)
//
//        currentBaseNote match {
//          case Some(m: ShortMessage) => {
//            val note = baseNote + scale((ccy - 48))
//
//            if (!notesOnCache(note) || (notesOnCache(note) && (currentTimeMillis() - timeLapsed) > 500)) {
//
//              if (currentTimeMillis() - timeLapsed > 100) {
//                notesOnCache.seq foreach (n => send(new ShortMessage(NOTE_OFF, 2, n, 0), 0))
//                notesOnCache = Set.empty
//              }
//
//              timeLapsed = currentTimeMillis()
//
//              send(new ShortMessage(NOTE_ON, 2, note, 64), 0)
//
//              notesOnCache = notesOnCache + note
//
//            }
//          }
//          case _ =>
//        }
//      }
//
//      case message: ShortMessage if (message.getCommand == CONTROL_CHANGE && message.getData1 == 1) => {
//        //println("Control change x: " + x.getData2);
//        send(new ShortMessage(PITCH_BEND, 2, 0, message.getData2 / 8), 0)
//      }
//
//      case _ => {
//
//        send(message, timeStamp)
//      }
//    }
//  }
//
//}