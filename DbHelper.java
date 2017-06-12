package testTander;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Logger;

/**
 * Created by F100D3R on 07.06.17.
 */
public class DbHelper {
    private Logger logger = Logger.getLogger(Program.class.getName());

    private String url;
    private String login;
    private String pw;

    // Записываем данные в БД
    public void writeDataToDb(Map<String, String> finalData, String column1, String column2, String agr){
        try {
            Connection conn = getConnection();
            logger.info("Открыто соединение с БД");
            Statement stat=conn.createStatement();
            stat.executeUpdate("CREATE TABLE F1_Result ("+column1+" VARCHAR(20), "+agr+"_"+column2+" VARCHAR(20))");
            logger.info("Создали таблицу");
            for (Map.Entry<String,String> entry:finalData.entrySet()
                    ) {
                stat.executeUpdate("INSERT INTO F1_Result VALUES ("+entry.getKey()+","+entry.getValue()+")");
            }
            logger.info("Данные внесены в таблицу");
            conn.close();
            System.out.println("Данные записаны в вашу БД");
        }catch (SQLException sqle){
            logger.warning("Ошибка при работе с БД");
            sqle.printStackTrace();
        }
    }

    // Считываем данные из файла и создаем по ним коннект к БД
    public Connection getConnection() throws SQLException
    {
        Properties props=new Properties();
        System.out.println("Введите путь к файлу-конфигу JDBC:");
        String fileNameProps = "";
        try {
            fileNameProps = ConsoleHelper.consoleInput();
        }catch (IOException ioe){
            logger.warning("Ошибка считывания названия файла");
            ioe.printStackTrace();
        }
        try(FileInputStream in=new FileInputStream(fileNameProps)){
            props.load(in);
        }catch (IOException ioe){
            logger.warning("Ошибка загрузки из файла Properties");
            ioe.printStackTrace();
        }
        String drivers = props.getProperty("jdbc.drivers");
        if(drivers !=null) System.setProperty("jdbc.drivers", drivers);
        url = props.getProperty("jdbc.url");
        login=props.getProperty("jdbc.username");
        pw=props.getProperty("jdbc.password");
        logger.info("Properties БД загружены");
        return DriverManager.getConnection(url, login, pw);
    }
}
