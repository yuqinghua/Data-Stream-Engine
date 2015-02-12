package com.mryu.flune.util;
import java.io.BufferedReader;  
import java.io.File;  
import java.io.FileReader;  
import java.io.IOException;  
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;  
import java.util.HashMap;  
import java.util.Iterator;  
import java.util.List;  
import java.util.Map;  
import java.util.Set;  
  
/** 
 * Simple reader methods for ini formated file using java 
 * Using UTF-8 encoding. 
 * 
 */  
public class IniReader {  
                    // section        item     value  
    private Map<String, HashMap<String, String>> sectionsMap = new HashMap<String, HashMap<String, String>>();  
                    //      item    value  
    private HashMap<String, String> itemsMap = new HashMap<String, String>();  
      
    private String currentSection = "";  
    private String fileName;
	private InputStream input;
  
    public IniReader(String fileName){
    	this.fileName = fileName;
    	loadDataFromFile();
    }
    
    public IniReader(InputStream input) {
		// TODO Auto-generated constructor stub
//    	BufferedReader reader = new BufferedReader(new InputStreamReader(input));

    	this.setInput(input);
    	loadDataFromInputStream();
	}

	/** 
     * Load data from target file 
     * @param file target file. It should be in ini format 
     */  
    private void loadDataFromFile() {
    	File file = new File(this.fileName);
    	if(!file.exists() || !file.canRead()){
    		return;
    	}
        BufferedReader reader = null;  
        try {  
            reader = new BufferedReader(new FileReader(file));  
            loadData(reader);
            reader.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
    }  
    

	/** 
     * Load data from target Stream 
     * @param file target file. It should be in ini format 
     */  
    private void loadDataFromInputStream() {
        BufferedReader reader = null;  
        try {  
        	reader = new BufferedReader(new InputStreamReader(input));
            loadData(reader);
            reader.close();  
        } catch (Exception e) {  
            e.printStackTrace();  
        } finally {  
            if (reader != null) {  
                try {  
                    reader.close();  
                } catch (IOException e1) {  
                    e1.printStackTrace();  
                }  
            }  
        }  
    }  
      
      
    private void loadData(BufferedReader reader) throws Exception {
    	String line = null;  
        String _char = "";
        String key = "";
        String value = "";
        while ((line = reader.readLine()) != null) {  
            if("".equals(line.trim())) continue;  
            if(line.startsWith("[") && line.trim().endsWith("]")) {
                // Ends last section  
                if(itemsMap.size() > 0 && !"".equals(currentSection.trim())) {  
            		if(key != ""){
            			itemsMap.remove(key);
                		itemsMap.put(key.trim(), value.trim());
            		}
                    sectionsMap.put(currentSection, itemsMap);  
                }  
                currentSection = "";  
                itemsMap = null;  
                key = "";
                value="";
                  
                // Start new section initial  
                currentSection = line.trim().substring(1, line.length() -1);  
                itemsMap = new HashMap<String, String>();   
            } else {  
                _char = "" + line.charAt(0);  
                if(_char.trim().equals("")){
                	value = value + "\n" + line;
                	itemsMap.remove(key);
                	itemsMap.put(key, value);
                }else{
                	int index = line.indexOf("=");  
                	if(index != -1) {  
                		if(key.trim() != ""){
                			itemsMap.remove(key);
                    		itemsMap.put(key.trim(), value.trim());
                		}
                		key = line.substring(0,index).trim();  
                		value = line.substring(index + 1, line.length());  
                		itemsMap.put(key, value);  
                	}
                }
            }  
//          System.out.println(line);  
        } 
        if(itemsMap.size() > 0 && !"".equals(currentSection.trim())) {  
            sectionsMap.put(currentSection, itemsMap);  
        }
	}

	public String getValue(String section, String item) {  
        HashMap<String, String> map = sectionsMap.get(section);  
        if(map == null) {  
            return null;
        }  
        String value = map.get(item);  
        return value;  
    }  
      
    public List<String> getSectionNames() {  
        List<String> list = new ArrayList<String>();  
        Set<String> key = sectionsMap.keySet();  
        for (Iterator<String> it = key.iterator(); it.hasNext();) {  
            list.add(it.next());  
        }  
        return list;  
    }  
      
    public Map<String, String> getItemsBySectionName(String section) {  
        return sectionsMap.get(section);  
    }

	public InputStream getInput() {
		return input;
	}

	public void setInput(InputStream input) {
		this.input = input;
	}  
}  