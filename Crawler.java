import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Crawler {  //ищет ссылки (сканер)
    public LinkedList<UrlDepthPair> proccessed;//List по ссылкам (хранит ссылки на предыдущий и следующий элемент)

    public Crawler() {
        proccessed = new LinkedList<>();  // хранит ссылку на предыдущий и следующий элемент
    }

    public void startParsing(URL baseUrl, int maxDepth, int currentDepth) {//начинаем парсить(сбор инфы)
        if (currentDepth > maxDepth) return;

        LinkedList<UrlDepthPair> links = getAllLinks(baseUrl, currentDepth);//записывает все эллементы (ссылки в новый лист)

        for (UrlDepthPair link: links) { // для нахождения  "подссылок"
            startParsing(link.realUrl, maxDepth, currentDepth + 1);
        }

        proccessed.addAll(links); //записывает все ссылки в лист proccessed из links
    }

    public static LinkedList<UrlDepthPair> getAllLinks(URL url, int depth) {
        try {
            LinkedList<UrlDepthPair> links = new LinkedList<>();

            int port = 80;
            String hostname = url.getHost(); //имя хоста

            Socket socket = new Socket(hostname, port);//Класс сокет реализует идею сокета, те каналы ввода/вывода, клиент общается с сервером
            socket.setSoTimeout(3000); // вызывает исключение если не поступают данные на вход

            OutputStream outStream = socket.getOutputStream(); // содержит ссылку на поток, в который будем записывать данные

            PrintWriter writer = new PrintWriter(outStream, true); // позволяет нам вывести информацию на консоль

            if (url.getPath().length() == 0) {  //вывод в консоль
                writer.println("GET / HTTP/1.1"); //если нет пары
                writer.println("Host: " + hostname);
                writer.println("Accept: text/html");
                writer.println("Accept-Language: en,en-US;q=0.9,ru;q=0.8");
                writer.println("Connection: close");
                writer.println();
            }
            else {
                writer.println("GET " + url.getPath() + " HTTP/1.1");  //есть пара
                writer.println("Host: " + hostname);
                writer.println("Accept: text/html");
                writer.println("Accept-Language: en,en-US;q=0.9,ru;q=0.8");
                writer.println("Connection: close");
                writer.println();
            }

            InputStream input = socket.getInputStream(); //позволяет открыть входной поток данных
            BufferedReader reader = new BufferedReader(new InputStreamReader(input)); //читает целую строку из потока ввода символов

            String htmlLine;

            Pattern patternURL = Pattern.compile(  //готовые к использованию решения часто возникающих в программировании задач.
                    //compile вызывает закрытый конструктор класса Pattern для создания скомпилированного представления(некого шаблона).(создания его в виде неизменяемого объекта)
                    "(href=\"http|href=\"https)://([\\w_-]+(?:(?:\\.[\\w_-]+)+))([\\w.,@?^=%&:/~+#-]*[\\w@?^=%&/~+#-])?"
            );

            while ((htmlLine = reader.readLine()) != null) { //считываем текст из потока
                Matcher matcherURL = patternURL.matcher(htmlLine); // определяет совпадения в водимой строке.
                while (matcherURL.find()) {//ищет совпадения
                    String link = htmlLine.substring(matcherURL.start() + 6,//считываем посимвольно
                            matcherURL.end()); // смещение после последнего совпавшего символа

                    links.add(new UrlDepthPair(link, depth));
                }
            }
            socket.close();

            return links;
        } catch (Exception e) {
            System.out.print(e.getMessage());  //если сработало исключение
            System.out.print(Arrays.toString(e.getStackTrace()));

            return new LinkedList<>();
        }
    }
}
