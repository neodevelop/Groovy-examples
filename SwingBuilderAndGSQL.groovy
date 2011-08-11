/**
* Usa cualquier base de datos en HSQLDB o de cualquier otro tipo,
* sólo ajusta la dependencia del conector, los parámetros de conexión
* y la consulta con la que obtienes los nombres de tabla de una base de datos
* según el manejador que estés usando....
*/

// Usamos la anotación @Grab para cargar la dependencia de HSQLDB
@GrabConfig(systemClassLoader=true)
@Grapes([@Grab(group='hsqldb', module='hsqldb', version='1.8.0.10')])

// Algunos imports
import groovy.swing.*
import javax.swing.*
import java.awt.BorderLayout as BL // Mira, imporst estáticos
import javax.swing.tree.DefaultMutableTreeNode as TreeNode

// Conectamos a la base de datos, en reallidad puedes usar la que tu quieras
def sql = groovy.sql.Sql.newInstance("jdbc:hsqldb:hsql://localhost/dbAsembly","sa","sa","org.hsqldb.jdbcDriver")
// Una consulta a cualquier tabla
def consulta = "Select * from cliente"
// Una consulta a las tablas de esta base de datos
def consultaTablas = "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.SYSTEM_TABLES WHERE TABLE_SCHEM='PUBLIC'"

def columnNames = []
// Este closure se ejecuta la primera ve después de la consulta
processMeta = { metaData ->
  columnNames = metaData.columnMetaData.collect{ column ->
    column.columnName
  }
}

def data = []
// Ejecutamos la consulta
sql.eachRow(consulta,processMeta){ registro -> // Me gusta que tan simple es ejecutar cualquier consulta e iterarla
  def registroEnMapa = [:]
  columnNames.each{ name ->
    registroEnMapa."$name" = registro["$name"] // Dinamismo en mapas
  }
  data << registroEnMapa
}

def nombresDeTabla = []
// Ejecutamos la consulta que obteien los nombres de todas la tablas de la BD
sql.eachRow(consultaTablas){ 
  nombresDeTabla << it['TABLE_NAME']
}

// Uf! un árbol de swing
JTree trainningTree
// Usamos un builder, característica poderosa de Groovy
swing = new SwingBuilder()
// Comenzamos a construir nuestra GUI
frame = swing.frame(
  title:'DB Poor Man',
  defaultCloseOperation: javax.swing.WindowConstants.EXIT_ON_CLOSE
) {
  borderLayout() // Así es! el mismo BorderLayout que ya conoces...
  scrollPane(constraints: BL.WEST, preferredSize: [160, -1]) { // scrollPane? claro JScrollPane!
      trainningTree = tree(rootVisible: true) // Adentro nuestro árbol
  }
  scrollPane(constraints:BL.CENTER) { // Este scrollPane en el centro
    table() { // Mostramos datos tabulares
      tableModel(list:data) { // Alimentamos la tabla
        columnNames.each{ columnName -> // Definimos las columnas que aparecerán
          propertyColumn(header:columnName, propertyName:columnName) // Goodness!!!
        }
      } 
    }
  } 
}
// borramos los elementos aactuales de nuestro árbol
trainningTree.model.root.removeAllChildren()
// Iteramos los nombres de las tablas
nombresDeTabla.each{
  def node = new TreeNode(it) //  para crear nodos
  trainningTree.model.root.add(node) // y agregarlos al árbol
}
trainningTree.model.reload(trainningTree.model.root) // Hacemos un refresh
frame.pack() 
frame.show() // Y mostramos....


