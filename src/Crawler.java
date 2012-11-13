
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.IOContext;
import org.apache.lucene.store.IndexInput;
import org.apache.lucene.store.IndexOutput;
import org.apache.lucene.util.Version;


public class Crawler {
	private LinkedList<String> URLList= new LinkedList<String>();
	private HashMap<String, Date> VisitedUrls = new HashMap<String, Date>();
	
	Analyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
	IndexWriterConfig iwc = new IndexWriterConfig(Version.LUCENE_40, analyzer);
	
	public void addURL(String url)
	{
		URLList.add(url);
	}
	
	public void printVisitedUrls()
	{
		for (String url : VisitedUrls.keySet())
		{
			System.out.println(url);
		}
	}
	
	public void start()
	{
		while (!URLList.isEmpty())
		{
			String url = URLList.removeFirst();
			
			// check if URL was visited
			if (!VisitedUrls.containsKey(url))
			{
				// debug
				System.out.println("Visiting " + url);
				
				String content = downloadURL(url);
				
				VisitedUrls.put(url, new Date()); // record that the url was visited, at the current time
				
				// TODO do something with content ...
				Document doc = new Document();
				
				try {
					iwc.setOpenMode(OpenMode.CREATE);
					
					String indexPath = "/home/burnde/index/";
					Directory dir = FSDirectory.open(new File(indexPath));
					
					System.out.println("Indexing to directory '" + indexPath + "'...");
					IndexWriter writer = new IndexWriter(dir,iwc);
					
					Field url_field = new StringField("URL", url, Field.Store.YES);
					doc.add(url_field);
					
					//Field url_title = new StringField("Title", url, Field.Store.YES);
					//doc.add(url_field);
					
					//Field url_content = new StringField("Content", content, Field.Store.YES);
					doc.add(new TextField("Contents", new StringReader(content)));
					//doc.add(url_content);
					
					if (writer.getConfig().getOpenMode() == OpenMode.CREATE) {
						// New index, so we just add the document (no old document can be there):
						System.out.println("adding " + url);
						writer.addDocument(doc);
					} else {
						// Existing index (an old copy of this document may have been indexed) so 
						// we use updateDocument instead to replace the old one matching the exact 
						// path, if present:
						System.out.println("updating " + url);
						//writer.updateDocument(new Term("path", file.getPath()), doc);
					}
					
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
				
				List<String> links = extractURLs(content);
				
				for (String link : links)
				{
					URLList.add(link);	
				}
			}
			else
			{
				System.out.println("Already visited " + url);
			}
		}
	}
	
	public List<String> extractURLs(String content)
	{
		int curIndex = 0;
		ArrayList<String> urls = new ArrayList<String>();
		while (true)
		{
			curIndex = content.indexOf("<a", curIndex);
			if (curIndex == -1)
				break; // No more links in this document
			
			// find the end tag
			int endTagIndex = content.indexOf(">", curIndex);
			if (endTagIndex == -1)
				break; // End tag not found, invalid HTML
			
			String element = content.substring(curIndex, endTagIndex);
			// find href attribute
			Pattern pattern = Pattern.compile("href=\"([^\"]+)\"");
			Matcher matcher = pattern.matcher(element);
			if (matcher.find())
			{
				// url is group 1 of the regex match
				String href = matcher.group(1);
				
				// add url to the list
				if (href != null)
				{
					// TODO pre-process url: e.g. add domain if missing?
					urls.add(href);
				}
			}
			
			
			curIndex = endTagIndex;
		}
		
		return urls;
	}
	
	public String downloadURL(String url) 
	{
		URL website;
		try {
			website = new URL(url);
			InputStream urlStream;
			try {
				urlStream = website.openStream();
				// thanks http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
				java.util.Scanner s = new java.util.Scanner(urlStream).useDelimiter("\\A");
			    return s.hasNext() ? s.next() : "";
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
		    
	}
	
	public static void main(String[] args) {
		Crawler crawly = new Crawler();
		crawly.addURL("https://www.udacity.com/cs101x/index.html");
		crawly.start();
		crawly.printVisitedUrls();
		
	}
}
