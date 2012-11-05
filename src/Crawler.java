
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Crawler {
	private LinkedList<String> URLList= new LinkedList<String>();
	private HashMap<String, Date> VisitedUrls = new HashMap<String, Date>();
	
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
