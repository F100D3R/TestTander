package testTander;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Created by F100D3R on 05.06.17.
 */
public class AgrHelper {
    private Logger logger = Logger.getLogger(Program.class.getName());
    // Высчитываем сумму по пунктам
    public Map<String, String> getSum(HashSet<TestRow> data, String column1, String column2){
        Map<String,String> temp = new HashMap<>();
        for (TestRow obj:data
             ) {
            if (temp.containsKey(obj.getRow().get(column1))){
                String val = (Integer.parseInt(temp.get(obj.getRow().get(column1))) + Integer.parseInt(obj.getRow().get(column2))) + "";
                temp.put(obj.getRow().get(column1),val);
            }else {
                temp.put(obj.getRow().get(column1),obj.getRow().get(column2));
            }
        }
        return temp;
    }

    // Высчитываем среднее по пунктам
    public Map<String, String> getAvg(HashSet<TestRow> data, String column1, String column2){
        Map<String,String> temp = new HashMap<>();
        // Запускаем потоки
        logger.info("Запускаем поток получения сумм");
        ThreadGetSum threadGetSum = new ThreadGetSum();
        threadGetSum.setData(data);
        threadGetSum.setColumn1(column1);
        threadGetSum.setColumn2(column2);
        threadGetSum.start();
        logger.info("Запускаем поток получения количества");
        ThreadGetCount threadGetCount = new ThreadGetCount();
        threadGetCount.setData(data);
        threadGetCount.setColumn1(column1);
        threadGetCount.start();
        // Ждем их завершения
        try{
            threadGetSum.join();
            logger.info("Поток суммы завершен");
        }catch (InterruptedException ie){
            ie.printStackTrace();
            logger.warning("Что-то не так с потоком получения суммы");
        }
        Map<String,String> dataSum = threadGetSum.getDataSum();
        try{
            threadGetCount.join();
            logger.info("Поток количества завершен");
        }catch (InterruptedException ie){
            ie.printStackTrace();
            logger.warning("Что-то не так с потоком получения количества");
        }
        Map<String,String> dataCount = threadGetCount.getDataCount();
        // Считаем
        for (Map.Entry<String,String> entry:dataSum.entrySet()
                ) {
            String val = (Float.parseFloat(dataSum.get(entry.getKey())) / Float.parseFloat(dataCount.get(entry.getKey()))) + "";
            temp.put(entry.getKey(),val);
        }
        return temp;
    }

    // Вычисляем количество пунктов
    public Map<String, String> getCount(HashSet<TestRow> data, String column1){
        Map<String,String> temp = new HashMap<>();
        for (TestRow obj:data
                ) {
            if (temp.containsKey(obj.getRow().get(column1))){
                String val = (Integer.parseInt(temp.get(obj.getRow().get(column1)))+1) + "";
                temp.put(obj.getRow().get(column1),val);
            }else {
                temp.put(obj.getRow().get(column1),"1");
            }
        }
        return temp;
    }

    // Вычисляем минимум каждого пункта
    public Map<String, String> getMin(HashSet<TestRow> data, String column1, String column2){
        Map<String,String> temp = new HashMap<>();
        for (TestRow obj:data
                ) {
            if (temp.containsKey(obj.getRow().get(column1))){
                if (Integer.parseInt(temp.get(obj.getRow().get(column1))) > Integer.parseInt(obj.getRow().get(column2))){
                    temp.put(obj.getRow().get(column1),obj.getRow().get(column2));
                }
            }else {
                temp.put(obj.getRow().get(column1),obj.getRow().get(column2));
            }
        }
        return temp;
    }

    // Вычисляем максимум каждого пункта
    public Map<String, String> getMax(HashSet<TestRow> data, String column1, String column2){
        Map<String,String> temp = new HashMap<>();
        for (TestRow obj:data
                ) {
            if (temp.containsKey(obj.getRow().get(column1))){
                if (Integer.parseInt(temp.get(obj.getRow().get(column1))) < Integer.parseInt(obj.getRow().get(column2))){
                    temp.put(obj.getRow().get(column1),obj.getRow().get(column2));
                }
            }else {
                temp.put(obj.getRow().get(column1),obj.getRow().get(column2));
            }
        }
        return temp;
    }

    // Поток получения суммы
    class ThreadGetSum extends Thread {
        private Map<String,String> dataSum;
        private HashSet<TestRow> data;
        private String column1;
        private String column2;

        public void setColumn1(String column1) {
            this.column1 = column1;
        }

        public void setColumn2(String column2) {
            this.column2 = column2;
        }

        public void setData(HashSet<TestRow> data) {
            this.data = data;
        }

        public Map<String, String> getDataSum() {
            return dataSum;
        }

        @Override
        public void run() {
            dataSum = getSum(data,column1,column2);
        }
    }

    // Поток получения количества
    class ThreadGetCount extends Thread {
        private Map<String,String> dataCount;
        private HashSet<TestRow> data;
        private String column1;

        public void setColumn1(String column1) {
            this.column1 = column1;
        }

        public void setData(HashSet<TestRow> data) {
            this.data = data;
        }

        public Map<String, String> getDataCount() {
            return dataCount;
        }

        @Override
        public void run() {
            dataCount = getCount(data,column1);
        }
    }
}
