import java.net.MalformedURLException;
import java.util.LinkedList;

public class CrawlerTask implements Runnable {//реализация интерфейса
    public UrlDepthPair depthPair;//Инициализация пар глубина+ссылка
    public UrlPool pool;//инициализация пула ссылок


    public CrawlerTask (UrlPool newPool) {
        pool = newPool;
    }

    public void run() {//Основной метод интерфейса Runnable, Поиск ссылок на странице в отдельном потоке
        depthPair = pool.get(); // получение пары из пула, ожидая в случае, если пара не будет сразу доступна.
        int depth = depthPair.depth;//Узнаем глубину
        LinkedList<UrlDepthPair> linksList = null;
        linksList = Crawler.getAllLinks(depthPair.realUrl, depth); //Получаем ссылку из пула строк
        for (UrlDepthPair newURL : linksList) {//перебираем все доступные ссылки, если они есть создаем пару
            UrlDepthPair newDepthPair = null;
            try {
                newDepthPair = new UrlDepthPair(newURL.url, depth + 1);//Главное условие Глубина +1 от предыдущей
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            pool.put(newDepthPair);
        }
    }
}
