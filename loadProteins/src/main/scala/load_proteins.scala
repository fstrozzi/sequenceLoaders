import scala.collection.JavaConversions._
import scalikejdbc._
import scala.util.parsing.combinator._
 
object FASTA {
 
  case class Entry( description: String, sequence: String )
 
  def fromFile( fn: String ): List[Entry] = {
    val lines = io.Source.fromFile(fn).getLines.mkString("\n")
    fromString( lines )
  }
 
  def fromString( input: String ): List[Entry] =
    Parser.parse(input)
 
  private object Parser extends RegexParsers {
    lazy val header = """>.*""".r ^^ { _.tail.trim }  
    lazy val seqLine = """[^>].*""".r ^^ { _.trim }
    lazy val sequence = rep1( seqLine ) ^^ { _.mkString }
    lazy val entry = header ~ sequence ^^ { 
      case h ~ s => Entry(h,s)
    }
    lazy val entries = rep1( entry )
    def parse( input: String ): List[Entry]  = {
      parseAll( entries, input ) match {
        case Success( es , _ ) => es
        case x: NoSuccess =>  throw new Exception(x.toString)
      }
    }
  }
}

object Main extends App {

  Class.forName("com.mysql.jdbc.Driver")
  ConnectionPool.singleton("jdbc:mysql://"+args(1), "XXX", "XXX")

  var sequences = scala.collection.mutable.ListBuffer[Seq[String]]()
  if(args.size == 0) {
    println("Please provide a Fasta file")
    scala.sys.exit(1)
  }
  val entries = FASTA.fromFile(args(0))
  entries.foreach {e =>
    val fastaHeader = e.description.split(" ")
    val seq_id = fastaHeader(0)
    val prokka_id = fastaHeader(1)
    val seq_description = fastaHeader.slice(2,fastaHeader.size).mkString(" ")
    val seq = e.sequence
    val seq_id_split = seq_id.split("_")
    val sample_id = seq_id_split.slice(1,seq_id_split.size-1).mkString("_")
    sequences.append(Seq(seq,sample_id,prokka_id))
    if(sequences.size == 10000) {
      DBcommit(sequences)
      sequences = scala.collection.mutable.ListBuffer[Seq[String]]()  
    } 
  }
  if(sequences.size > 0) {
    DBcommit(sequences) 
  }

  def DBcommit(sequences: scala.collection.mutable.ListBuffer[Seq[String]]) {
    DB autoCommit { implicit session =>
      sql"UPDATE genes SET aminoacidic_sequence=? WHERE sample_id=? AND prokka_id=?".batch(sequences: _*).apply()
    } 
  }
}
