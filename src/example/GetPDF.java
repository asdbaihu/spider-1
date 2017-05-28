package example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPDF {
	// 使用队列保存 URL
	static Queue<String> urlQueue = new LinkedList<>();
	// 保存PDF的URL集合
	static HashSet<String> pdfUrlSet = new HashSet<>();
	static int i = 1;

	// 获取网页的URL
	public static void getUrl(String target, String rootUrl) {
		try {
			URL url = new URL(target);
			URLConnection conn = url.openConnection();
			String regex = "[\\w+\\.?/?]+\\.(htm[l]*|jsp)";
			Pattern p = Pattern.compile(regex);
			// 获取字节流放入缓存中
			BufferedReader bReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String buf = null;
			while ((buf = bReader.readLine()) != null) {
				Matcher buf_m = p.matcher(buf);
				while (buf_m.find()) {
					String urlString = buf_m.group();
					if (urlString.startsWith("www")) {
						urlString = "http://" + urlString;
					} else if (urlString.startsWith("//")) {
						urlString = "http:" + urlString;
					} else if (urlString.startsWith("/")) {
						urlString = rootUrl + urlString;
					} else {

					}
					// 添加进队列
					urlQueue.offer(urlString);
				}
			}
			bReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// 获取PDF的URL
	public static void getPDFUrl(String url, String rootUrl) {
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		try {
			URL rawURL = new URL(url);
			// 建立连接
			URLConnection conn = rawURL.openConnection();
			// 获取网页字节流
			InputStream is = conn.getInputStream();
			// 转化成字符流
			InputStreamReader inputStreamReader = new InputStreamReader(is, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			// 字符流转string
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			is.close();
			// PDF 正则表达式
			String regex = "[\\w+\\.?/?-]+\\.pdf";
			Pattern pattern = Pattern.compile(regex);
			// 找匹配项
			Matcher matcher = pattern.matcher(stringBuffer);
			while (matcher.find()) {
				pdfUrlSet.add(rootUrl + matcher.group());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// 下载 PDF
	public static void getPDF(String url) {
		try {
			URL urlP = new URL(url);
			URLConnection conn = urlP.openConnection();
			InputStream inputStream = conn.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			byte[] buf = new byte[10240];
			int size = 0;

			// 创建 file 文件夹
			File file = new File("./file");
			if (!file.exists()) {
				file.mkdir();
			}
			FileOutputStream fos = new FileOutputStream("./file/" + i + ".pdf");
			while ((size = bufferedInputStream.read(buf)) != -1) {
				fos.write(buf, 0, size);
			}
			fos.close();
			bufferedInputStream.close();

		} catch (IOException e) {
			e.printStackTrace();
		}
		i++;
	}

	public static void width() {
		String rootUrl = "http://pg.njupt.edu.cn";
		System.out.println("start...");
		getUrl(rootUrl, rootUrl);
		// 首页的URL队列
		int size0 = urlQueue.size();
		while (size0 > 0) {
			getUrl(urlQueue.poll(), rootUrl);
			// System.out.println(size0);
			size0--;
		}
		// 进行一次广度遍历
		int size1 = urlQueue.size();
		while (size1 > 0) {
			getPDFUrl(urlQueue.poll(), rootUrl);
			size1--;
		}
		System.out.println(pdfUrlSet.size());
		// 遍历PDF的URL集合，进行下载操作
		for (String string : pdfUrlSet) {
			getPDF(string);
		}
		System.out.println("end!");

	}

	public static void main(String[] args) {
		width();
	}

}
