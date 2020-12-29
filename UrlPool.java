import java.util.ArrayList;
import java.util.LinkedList;

public class UrlPool {
    private final LinkedList<UrlDepthPair> pendingURLs; //Неизменяемый лист для рассмотренных ссылок
    public LinkedList<UrlDepthPair> processedURLs;//Лист для ссылок в процессе
    private final ArrayList<String> seenURLs = new ArrayList<>();
    public int waitingThreads;
    int maxDepth;

    public UrlPool(int maxDepthPair) {
        maxDepth = maxDepthPair;
        waitingThreads = 0;
        pendingURLs = new LinkedList<>();//Рассмотренные
        processedURLs = new LinkedList<>();//В процессе
    }
    public synchronized int getWaitThreads() {//Пока ресурсом пользуется поток, другие ждут
        return waitingThreads;
    }

    public synchronized int size() { return pendingURLs.size(); }//Кол-во ожидающих ссылок

    public synchronized void put(UrlDepthPair depthPair) {//Добавление ссылки в пул
        if (waitingThreads != 0) {
            --waitingThreads;
            this.notify();  // позволяет продолжить работу потока, у которого раннее был вызван wait
        }
        if (!seenURLs.contains(depthPair.url) &
                !pendingURLs.contains(depthPair)) { //contains проверяет не сожержит ли наш лист пару
            if (depthPair.depth < maxDepth) { // если меньше макс глубины
                pendingURLs.add(depthPair);  // то добавить в лист
            }
            else {
                processedURLs.add(depthPair); // иначе добавляем в другой лист
                seenURLs.add(depthPair.url); // добавляем в array
            }
        }
    }
    public synchronized UrlDepthPair get() {//Получение из пула ссылки
        UrlDepthPair myDepthPair;
        while (pendingURLs.isEmpty()) {    // пока наш список не содержит элементов
            waitingThreads++;              // увеличиваем  ожидание потока
            try {
                this.wait();               // переводим потом в режим ожидания, пока другой поток не вызовет notify
            }
            catch (InterruptedException e) {
                System.err.println("MalformedURLException: " + e.getMessage());   // исключение
                return null;
            }
        }
        myDepthPair = pendingURLs.pop();      // удаляет последний добавленный объект и возвращает этот объект в качестве значения

        while (seenURLs.contains(myDepthPair.url)) {  // пока наш лист содержит ссылку

            myDepthPair = pendingURLs.pop();          // убираем последний добваленный объект
        }

        processedURLs.add(myDepthPair);               // добавляем в лист
        seenURLs.add(myDepthPair.url);

        return myDepthPair;
    }
}
