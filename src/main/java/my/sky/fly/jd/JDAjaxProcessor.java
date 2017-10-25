package my.sky.fly.jd;

import us.codecraft.webmagic.Page;
import us.codecraft.webmagic.Request;
import us.codecraft.webmagic.Site;
import us.codecraft.webmagic.Spider;
import us.codecraft.webmagic.downloader.selenium.SeleniumDownloader;
import us.codecraft.webmagic.pipeline.FilePipeline;
import us.codecraft.webmagic.processor.PageProcessor;
import us.codecraft.webmagic.selector.Html;
import us.codecraft.webmagic.selector.Selectable;

import java.net.URL;
import java.util.*;

import static us.codecraft.webmagic.selector.Selectors.xpath;


/**
 * my.sky.fly.jd
 * webmagictest
 * 2017/10/24
 * zhangyufei05@countrygarden.com.cn
 */

public class JDAjaxProcessor implements PageProcessor {
    public static final String DOMAIN_NAME = "https://search.jd.com/";
    public static final String URL_LIST = "https://search.jd.com/search?keyword=%E5%86%85%E5%AD%98%E6%9D%A1&enc=utf-8&qrst=1&rt=1&stop=1&vt=2&wq=%E5%86%85%E5%AD%98%E6%9D%A1";
    public static Set<String> urls = new HashSet();
    public static Set<String> ramIds = new HashSet();

    private Site site = Site.me().setCycleRetryTimes(5).setRetryTimes(5).setSleepTime(500).setTimeOut(3 * 60 * 1000)
            .setUserAgent("Mozilla/5.0 (Windows NT 6.1; WOW64; rv:38.0) Gecko/20100101 Firefox/38.0")
            .addHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8")
            .addHeader("Accept-Language", "zh-CN,zh;q=0.8,en-US;q=0.5,en;q=0.3")
            .setCharset("UTF-8");


    public static void main(String[] args) {
        String s = ClassLoader.getSystemResource("").getPath();
        String configPath = s + "webmagic/selenium/config.ini";
        System.setProperty("selenuim_config", configPath);
        Spider.create(new JDAjaxProcessor())
                .addUrl(URL_LIST)
                .addPipeline(new FilePipeline("D:\\webmagic\\"))
                .setDownloader(new SeleniumDownloader("E:\\work\\chromecore\\chromedriver.exe"))
                .run();
    }


    public Site getSite() {
        return site;
    }

    @Override
    public void process(Page page) {
        //只关心频率和平台筛选条件
        if(page.getUrl().toString().equals(URL_LIST)){
            processRAMHeadUrl(page);
        }else{
            //模糊搜索
            //全是内存条信息
            //需要去重 1.url 防止循环引用 2.内存条信息重复 3.必须是自营商品
            processRAMUrl(page);
        }
    }

    private void processRAMUrl(Page page) {
        processRAMHeadUrl(page);
        //商品
        List<String> selectsFilters = page.getHtml().xpath("//div[@id='J_goodsList']/ul/li").all();
        //抓取分页数据
        String s = page.getHtml().xpath("//div[@id='J_bottomPage']/span/em/b").toString();
        System.out.println(selectsFilters.size());
    }

    private void processRAMHeadUrl(Page page) {
        List<String> selectsFilters = page.getHtml().xpath("//a[@href]").all();
        //只关注内存的url
        for (String selectFilter : selectsFilters) {
            Html selectFilterHtml = new Html(selectFilter);
            String link = selectFilterHtml.xpath("//a[@href]/@href").toString();
            if(link.contains("search?keyword=%E5%86%85%E5%AD%98%E6%9D%A1")){
                if(urls.contains(link)){
                    continue;
                }else{
                    urls.add(link);
                }
                link = DOMAIN_NAME+link;
                String title = selectFilterHtml.xpath("//a[@href]/text()").toString();
                Request request = new Request(link).setPriority(0).putExtra("province", title);
                page.addTargetRequest(request);

            }
        }
    }

    public void writeFile(){

    }
}
