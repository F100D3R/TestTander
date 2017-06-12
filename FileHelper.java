package testTander;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by F100D3R on 06.06.17.
 */
public class FileHelper {
    private Logger logger = Logger.getLogger(Program.class.getName());

    private static String fileSuffix;
    private static String fileName = "result.csv";
    // Читаем данные из файла
    public HashSet<TestRow> getDataFromFile(){
        HashSet<TestRow> temp = new HashSet<>();
        fileName = askFileName();
        fileSuffix = fileName.substring(fileName.length()-4).toLowerCase();
        switch (fileSuffix){
            case ".csv":
                // Читаем CSV
                temp = readDataFromCSV(fileName);
                break;
            case ".xml":
                // Читаем XML
                temp = readDataFromXML(fileName);
                break;
            default:
                System.out.println("Неверный формат исходного файла. Запустите программу с верными параметрами!");
                logger.warning("С таким форматом данных программа не работает :(");
                System.exit(2);
        }
        return temp;
    }

    // Запрашиваем имя файла
    private String askFileName(){
        String file = new String();
        System.out.println("Введите корректный путь и имя файла:");
        try {
            while (true){
                file = ConsoleHelper.consoleInput();
                if (file.toLowerCase().equals("exit")) System.exit(2);
                if (file.length()>4) break;
                System.out.println("Слишком короткий путь файла. Попробуйте заново или наберите слово exit для выхода из программы");
            }
        }catch (IOException ioe){
            logger.warning("Ошибка считывания названия файла");
            ioe.printStackTrace();
        }
        return file;
    }

    // Читаем данные из CSV файла
    private HashSet<TestRow> readDataFromCSV(String fileName){
        HashSet<TestRow> temp = new HashSet<>();
        String line;
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))){
            reader.readLine();
            while ((line = reader.readLine()) != null){
                String[] row = line.split(",");
                temp.add(new TestRow(row[0],row[1],row[2],row[3],row[4],row[5],row[6]));
            }
        }catch (FileNotFoundException fnfe){
            logger.warning("Такой файл не найден...");
            fnfe.printStackTrace();
        }catch (IOException ioe){
            logger.warning("Ошибка при чтении из файла");
            ioe.printStackTrace();
        }
        return temp;
    }

    // Читаем данные из XML файла
    private HashSet<TestRow> readDataFromXML(String fileName){
        HashSet<TestRow> temp = new HashSet<>();
        try{
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document document = documentBuilder.parse(fileName);
            Node root = document.getDocumentElement();
            NodeList rows = root.getChildNodes();
            for (int i = 0; i < rows.getLength(); i++) {
                Node row = rows.item(i);
                if (Node.ELEMENT_NODE == row.getNodeType()) {
                    Element element = (Element) row;
                    temp.add(new TestRow(element.getElementsByTagName("start_page").item(0).getTextContent(),
                            element.getElementsByTagName("user").item(0).getTextContent(),
                            element.getElementsByTagName("ts").item(0).getTextContent(),
                            element.getElementsByTagName("depth").item(0).getTextContent(),
                            element.getElementsByTagName("duration").item(0).getTextContent(),
                            element.getElementsByTagName("transmit").item(0).getTextContent(),
                            element.getElementsByTagName("type").item(0).getTextContent()));
                }
            }
        }catch (ParserConfigurationException pce){
            pce.printStackTrace();
        }catch (SAXException saxe){
            saxe.printStackTrace();
        }catch (IOException ioe){
            ioe.printStackTrace();
        }
        return temp;
    }

    // Пишем данные в файл
    public void writeFile(Map<String, String> finalData, String column1, String column2, String agr){
        fileSuffix = fileName.substring(fileName.length()-4).toLowerCase();
        switch (fileSuffix){
            case ".csv":
                // Пишем в CSV
                writeToCSV(finalData,column1,column2,agr,fileName);
                break;
            case ".xml":
                // Пишем в XML
                writeToXML(finalData,column1,column2,agr,fileName);
                break;
            default:
                System.out.println("Неверный формат файла вывода. Запустите программу с верными параметрами!");
                System.exit(2);
        }
    }

    // Пишем в CSV файл
    private void writeToCSV(Map<String, String> finalData, String column1, String column2, String agr, String fileName){
        String head = column1 + "," + agr + "_" + column2;
        String newFileName = new StringBuffer(fileName).insert(fileName.lastIndexOf("\\")+1,"new_").toString();
        try(PrintWriter pw = new PrintWriter(new OutputStreamWriter(new FileOutputStream(newFileName)))){
            pw.println(head);
            for (Map.Entry<String,String> entry:finalData.entrySet()
                    ) {
                pw.println(entry.getKey() + "," + entry.getValue());
            }
        }catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }
        System.out.println("Данные записаны в файл " + newFileName);
    }

    // Пишем в XML файл
    private void writeToXML(Map<String, String> finalData, String column1, String column2, String agr, String fileName){
        String newFileName = new StringBuffer(fileName).insert(fileName.lastIndexOf("\\")+1,"new_").toString();
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            Document doc = documentBuilder.newDocument();
            Element root = doc.createElement("root");
            doc.appendChild(root);
            for (Map.Entry<String,String> entry:finalData.entrySet()
                    ) {
                Element row = doc.createElement("row");
                Element col1 = doc.createElement(column1);
                col1.setTextContent(entry.getKey());
                row.appendChild(col1);
                Element col2 = doc.createElement(agr+"_"+column2);
                col2.setTextContent(entry.getValue());
                row.appendChild(col2);
                root.appendChild(row);
            }
            Transformer tr = TransformerFactory.newInstance().newTransformer();
            DOMSource source = new DOMSource(doc);
            FileOutputStream fos = new FileOutputStream(newFileName);
            StreamResult result = new StreamResult(fos);
            tr.transform(source, result);
        }catch (ParserConfigurationException pce){
            pce.printStackTrace();
        }catch (TransformerConfigurationException tce){
            tce.printStackTrace();
        }catch (FileNotFoundException fnfe){
            fnfe.printStackTrace();
        }catch (TransformerException te){
            te.printStackTrace();
        }
        System.out.println("Данные записаны в файл " + newFileName);
    }
}
