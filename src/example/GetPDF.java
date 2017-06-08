package example;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Queue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class GetPDF {
	// ʹ�ö��кͼ��ϱ��� URL�Լ�PDFUrl
	static Queue<String> urlQueue = new LinkedList<>();
	static HashSet<String> urlSet = new HashSet<>();
	static HashSet<String> pdfUrlSet = new HashSet<>();
	static Queue<String> pdfUrlQueue = new LinkedList<>();
	// �ļ��������
	static int i = 1;

	/*
	 * ʹ��socket�������ӣ���������ҳԴ�롣 �������ʵ���վ�Ĵ������Ϊ chunked ��
	 * Transfer-Encoding:chunked���޷���ȷ������ҳԴ�룬��ʱ�����ᴦ��
	 * �������´���ʹ���˷�װ��URLConnection�������ӡ� ������ʦ����!
	 */

	public static String socketConn(String host) {
		String line = "";
		StringBuffer sb = new StringBuffer();
		try {
			InetAddress addr = InetAddress.getByName(host);// ��ȡIP��ַ
			Socket sock = new Socket(addr, 80);
			BufferedWriter wr = new BufferedWriter(new OutputStreamWriter(sock.getOutputStream(), "utf-8"));
			wr.write("GET " + '/' + " HTTP/1.1\r\n");
			wr.write("HOST:" + host + "\r\n");
			wr.write("Accept:*/*\r\n");
			wr.write("\r\n\r\n");
			wr.flush();

			BufferedReader rd = new BufferedReader(new InputStreamReader(sock.getInputStream()));
			while ((line = rd.readLine()) != null) {
				sb.append(line);
			}
			sock.close();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return sb.toString();
	}

	// ��ȡ��ҳ��URL
	public static void getUrl(String target, String rootUrl) {
		try {
			URL url = new URL(target);
			URLConnection conn = url.openConnection();
			String regex = "[\\w+\\.?/?]+\\.(htm[l]*|jsp)";
			Pattern p = Pattern.compile(regex);
			// ��ȡ�ֽ������뻺����
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
					// ��������в�����URL��������
					if (!urlSet.contains(urlString)) {
						// ��ӽ�����
						urlSet.add(urlString);
						urlQueue.offer(urlString);
					}
				}
			}
			bReader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// ��ȡPDF��URL
	public static void getPDFUrl(String url, String rootUrl) {
		StringBuffer stringBuffer = new StringBuffer();
		String line = null;
		try {
			URL rawURL = new URL(url);
			// ��������
			URLConnection conn = rawURL.openConnection();
			// ��ȡ��ҳ�ֽ���
			InputStream is = conn.getInputStream();
			// ת�����ַ���
			InputStreamReader inputStreamReader = new InputStreamReader(is, "utf-8");
			BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
			// �ַ���תstring
			while ((line = bufferedReader.readLine()) != null) {
				stringBuffer.append(line);
			}
			bufferedReader.close();
			inputStreamReader.close();
			is.close();
			// PDF ������ʽ
			String regex = "[\\w+\\.?/?-]+\\.pdf";
			Pattern pattern = Pattern.compile(regex);
			// ��ƥ����
			Matcher matcher = pattern.matcher(stringBuffer);
			while (matcher.find()) {
				// ��������в����ڸ�url����������
				if (!pdfUrlSet.contains(rootUrl + matcher.group())) {
					pdfUrlSet.add(rootUrl + matcher.group());
					pdfUrlQueue.offer(rootUrl + matcher.group());
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// ���� PDF
	public static void getPDF(String url) {
		try {
			URL urlP = new URL(url);
			URLConnection conn = urlP.openConnection();
			InputStream inputStream = conn.getInputStream();
			BufferedInputStream bufferedInputStream = new BufferedInputStream(inputStream);

			byte[] buf = new byte[10240];
			int size = 0;

			// ���� file �ļ���
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
		// �߳�1������URL
		Thread t1 = new Thread() {

			@Override
			public void run() {
				while (true) {
					if (!urlQueue.isEmpty()) {
						getUrl(urlQueue.peek(), rootUrl);
						getPDFUrl(urlQueue.peek(), rootUrl);
						System.out.println(urlQueue.peek());
						urlQueue.poll();
					} else {
						break;
					}
				}
			}
		};
		// �߳�2��������PDF
		Thread t2 = new Thread() {

			@Override
			public void run() {
				while (true) {
					if (pdfUrlQueue.isEmpty()) {
						try {
							sleep(2);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						continue;
					}
					System.out.println(pdfUrlQueue.peek());
					getPDF(pdfUrlQueue.poll());
				}

			}
		};
		t1.start();
		t2.start();
	}

	public static void main(String[] args) {
		width();
	}

}
