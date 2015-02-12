package com.mryu.flune.util;

import com.mryu.flune.core.ContextConfigure;

public class TagDecoder {
	
	public static String decodeSystemTag(String tagString, ContextConfigure configure) {
		if(tagString == null || tagString.trim().equals("")){
			return null;
		}
		
		String[] arr = getVariable(tagString, "", "$", '{', '}');
		while(true){
			if(arr[0] == null){
				break;
			}
			tagString = tagString.replace(arr[0], arr[1]);
			
			arr = getVariable(tagString, "", "$", '{', '}');
		}
		
		return tagString;
	}
	
	
	public static void main(String[] args) {
		String tagString = "${yesterday}";
		String[] arr = getVariable(tagString, "", "$", '{', '}');
		while(true){
			if(arr[0] == null){
				break;
			}
			tagString = tagString.replace(arr[0], arr[1]);
			
			arr = getVariable(tagString, "", "$", '{', '}');
		}
		System.out.println(tagString);
	}
	
	/**
	 * example : $string{abc}  [command:..$string{abc}...,
	 * key:string, keyPre:$, varPre:{, varSuf:}]
	 * @param command
	 * @param key
	 * @param keyPre
	 * @param varPre
	 * @param varSuf
	 * @return
	 */
	public static String[] getVariable(String command, String key, String keyPre, char varPre, char varSuf) {
		key = keyPre + key + varPre;
		key = key.toUpperCase();
		String temp = command;
		command = command.toUpperCase();
		if (command.indexOf(key) != -1) {
			int index = command.indexOf(key);
			int rIndex = command.indexOf(varSuf, index);
			String mm = command.substring(index, rIndex + 1);
			int lIndex = command.indexOf(varPre, index);
			String tmm = mm.substring(mm.indexOf(varPre) + 1, mm.indexOf(varSuf));
			while (tmm.indexOf(varPre) != -1) {
				lIndex = command.indexOf(varPre, lIndex + 1);
				rIndex = command.indexOf(varSuf, rIndex + 1);
				if (rIndex == -1) {
					return new String[]{null, null};
				}
				tmm = command.substring(lIndex + 1, rIndex);
			}
			command = temp;
			String _str = command.substring(index, rIndex + 1).trim();
			return new String[] { _str,
					_str.substring(key.length(), _str.length() - 1) };
		} else {
			return new String[]{null, null};
		}
	}
	
	/**
	 * example : $string{abc}  [command:..$string{abc}...,
	 * key:string, keyPre:$, varPre:{, varSuf:}]
	 * @param command
	 * @param key
	 * @param keyPre
	 * @param varPre
	 * @param varSuf
	 * @return
	 */
	public static String[] getVariable(StringBuffer command, String key, String keyPre, char a_varPre, char a_varSuf) {
		String varPre = "" + a_varPre;
		String varSuf = "" + a_varSuf;
		key = (keyPre + key + varPre).toUpperCase();
		key = key.toUpperCase();
		StringBuffer temp = command;
		command = new StringBuffer(command.toString().toUpperCase());
		if (command.indexOf(key) != -1) {
			int index = command.indexOf(key);
			int rIndex = command.indexOf(varSuf, index);
			String mm = command.substring(index, rIndex + 1);
			int lIndex = command.indexOf(varPre, index);
			String tmm = mm.substring(mm.indexOf(varPre) + 1, mm.indexOf(varSuf));
			while (tmm.indexOf(varPre) != -1) {
				lIndex = command.indexOf(varPre, lIndex + 1);
				rIndex = command.indexOf(varSuf, rIndex + 1);
				if (rIndex == -1) {
					return new String[]{null, null};
				}
				tmm = command.substring(lIndex + 1, rIndex);
			}
			command = temp;
			String _str = command.substring(index, rIndex + 1).trim();
			return new String[] { _str,
					_str.substring(key.length(), _str.length() - 1) };
		} else {
			return new String[]{null, null};
		}
	}

	
}
