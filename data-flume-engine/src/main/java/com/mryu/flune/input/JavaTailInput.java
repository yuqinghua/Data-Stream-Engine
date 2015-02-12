package com.mryu.flune.input;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.RandomAccessFile;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitOption;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardWatchEventKinds;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.management.RuntimeErrorException;

import net.sf.json.JSONArray;

import org.apache.log4j.Logger;

import com.mryu.flune.core.AbstractInput;
import com.mryu.flune.input.tail.LogFinder;
import com.mryu.flune.input.tail.RefreshPositionRunnable;
import com.mryu.flune.input.tail.RefreshWatchRunnable;
import com.mryu.flune.input.tail.WatchKeyCleanRunnable;
import com.mryu.flune.util.NioFileReader;

public class JavaTailInput extends AbstractInput{

	private static Logger logger = Logger.getLogger(JavaTailInput.class);
	

	// , "C:/Users/Lucas/*/mm/*.log"
	// private static final String[] logRoots =
	// {"C:/Users/Lucas/Desktop/*.log"};
	// "C:/Users/Lucas/Desktop/mm/*.log",
	private String[] logPathPattern = { "C:\\Users\\Lucas\\Desktop\\mm.log" };
	// private static final String logFormularExpress = ".*\\.log";

	public String posPath = "C:/Users/Lucas/Desktop/tail.pos";

	long MAX_DELAY = 3600000; // one hour

	/**
	 * Path: /log/kuku/20001/20141121.log LogFinder: to matches the event path.
	 */
	public Map<String, LogFinder> logFinderMap = new HashMap<String, LogFinder>();

	/**
	 * Path: /log/kuku/20001/20141121.log Long: position of File has been read!
	 */
	public Map<String, Long> positionMap;

	/**
	 * Path: /log/kuku/20001/20141121.log
	 */
	// static Map<Path, RandomAccessFile> readerMap = new HashMap<Path,
	// RandomAccessFile>();
	/**
	 * String:log short name 20141121.log, Path:parentDir /log/kuku/20001 Long:
	 * time stamp of last read
	 */
	Map<String, Map<Path, Long>> filenameMap = new HashMap<String, Map<Path, Long>>();

	/**
	 * watchKey is used to used to cancel to watch path
	 */
	Map<String, WatchKey> dirWatchKeyMap = new HashMap<String, WatchKey>();

	/**
	 * used for scheduler to cancel watching from path.
	 */
	ExecutorService watchKeyCleanService;

	/**
	 * used for scheduler to scan the file system with logPathPattern
	 */
	ExecutorService refreshWatchService;

	ExecutorService refreshPositionService;

	final Format dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	WatchService watcher;

	int isInitailized = 0;

	public ReadMode readMode = ReadMode.APPEND_ONLY;

	public static String FileReadOnlyMode = "r";
	
	@Override
	protected void afterInitialize() {
		Map<String, String> config = configure.getReader().getItemsBySectionName(inputName);
		JSONArray arr = JSONArray.fromObject(config.get("logPathPattern"));
		this.logPathPattern = new String[arr.size()];
		arr.toArray(logPathPattern);
		this.posPath = config.get("posPath") == null ? this.getClass()
				.getClassLoader().getResource("")
				+ File.separator + "tail.pos" : config.get("posPath");
		try {
			if(config.get("readMode") != null){this.readMode = ReadMode.valueOf(config.get("readMode").trim());}
		} catch (Exception e) {
			logger.warn("INPUT[" + inputName + "] readModereadMode not usable, readModereadMode:" + config.get("readMode"));
		}
		
		this.readMode = this.readMode == null ? ReadMode.APPEND_ONLY : this.readMode;
		super.afterInitialize();
	}
	
	/**
	 * @param args
	 * @throws IOException
	 * @throws InterruptedException
	 */
	@Override
	public void run() {
		try {
			watcher = FileSystems.getDefault().newWatchService();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		readPosition(posPath);
		initialize();
		isInitailized = 1;
		startWatchKeyCleanService();
		startRefreshWatchService();
		initialLastPositionAll();
		startRefreshPositionService();
		long position = 0;
		WatchKey key = null;
		RandomAccessFile reader = null;
		while (isRunning) {
			if (watcher == null) {
				return;
			}
			// System.out.println("take begin...");
			try {
				key = watcher.take();
			} catch (InterruptedException e1) {
				e1.printStackTrace();
				System.exit(-1);
			}
			// System.out.println("take end...");
			for (WatchEvent<?> event : key.pollEvents()) {
				WatchEvent.Kind<?> kind = event.kind();

				if (kind == StandardWatchEventKinds.ENTRY_MODIFY || kind == StandardWatchEventKinds.ENTRY_CREATE) {
					@SuppressWarnings("unchecked")
					WatchEvent<Path> ev = (WatchEvent<Path>) event;
					Path logFile = ev.context();
					List<Path> children = resolvePath(logFile);
					if (children == null || children.size() == 0) {
						continue;
					}
					for (Path child : children) {
						if (child == null) {
							continue;
						}
						position = positionMap.containsKey(child.toString()) ? positionMap
								.get(child.toString()) : 0;
						try {
							reader = new RandomAccessFile(child.toFile(),
									FileReadOnlyMode);
							// readerMap.put(child, reader);

							readPostition(child, reader, position);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							if (reader != null) {
								try {
									reader.close();
								} catch (Exception e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}

					}
				}
//				else if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
//					@SuppressWarnings("unchecked")
//					WatchEvent<Path> ev = (WatchEvent<Path>) event;
//					Path logFile = ev.context();
//					System.out.println("======================================c: " + logFile);
					// List<Path> children = resolvePath(logFile);
					// if(children == null || children.size() == 0){
					// continue;
					// }
					// for(Path child : children){
					// if(child == null){
					// continue;
					// }
					// if(!positionMap.containsKey(child)){
					// initialLastPosition(ReadMode.FROM_BEGIN, child);
					// }
					// }
//				}
			}
			if (!key.reset()) {
				System.err.println("key.reset() returnabc false");
				break;
			}

			// System.out.println("GC....");
			System.gc();
		}
	}

	@SuppressWarnings("unchecked")
	private void readPosition(String posPath) {
		ObjectInputStream oin = null;
		try {
			File file = new File(posPath);
			if (!file.exists()) {
				file.createNewFile();
			}
			oin = new ObjectInputStream(new FileInputStream(file));
			positionMap = (Map<String, Long>) oin.readObject();

		} catch (Exception e) {
			logger.info("Position file is not found, it would be created!");
//			e.printStackTrace();
		} finally {
			if (positionMap == null) {
				positionMap = new HashMap<String, Long>();
			}
			if(oin != null){
				try {
					oin.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void initialLastPositionAll() {
		Path path;
		for (Entry<String, Map<Path, Long>> entry : filenameMap.entrySet()) {
			for (Entry<Path, Long> pathEntry : entry.getValue().entrySet()) {
				path = pathEntry.getKey().resolve(entry.getKey());
				initialLastPosition(path);
			}
		}
	}

	private void initialLastPosition(Path path) {
		initialLastPosition(readMode, path);
	}

	private void initialLastPosition(String... paths) {
		for (String path : paths) {
			initialLastPosition(new File(path).toPath());
		}
	}

	private void initialLastPosition(ReadMode readMode, String... paths) {
		for (String path : paths) {
			initialLastPosition(readMode, new File(path).toPath());
		}
	}

	private void initialLastPosition(ReadMode readMode, Path... paths) {
		for (Path path : paths) {
			try {
				if (!positionMap.containsKey(path.toString())) {
					switch (readMode) {
					case FROM_BEGIN:
						positionMap.put(path.toString(), 0L);
						break;
					default:
						positionMap
								.put(path.toString(), path.toFile().length());
						break;
					}
				}
				readFileWithStartPosition(path);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private void readFileWithStartPosition(Path path) {

		if (!positionMap.containsKey(path.toString())) {
			System.out.println("Path: " + path
					+ " is not found in positionMap.");
			return;
		}
		synchronized (path.toString().intern()) {
			if (positionMap.get(path.toString()) < path.toFile().length()) {
				RandomAccessFile reader = null;
				try {
					reader = new RandomAccessFile(path.toFile(),
							FileReadOnlyMode);
					// readerMap.put(path, reader);
					readPostition(path, reader,
							positionMap.get(path.toString()));
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} finally {
					if (reader != null) {
						try {
							reader.close();
						} catch (IOException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}

	/**
	 * read log and consume it
	 * @param path
	 * @param reader
	 * @param position
	 */
	private void readPostition(Path path, RandomAccessFile reader, long position) {
		NioFileReader fReader = null;
		try {
			String line;
			reader.seek(position);
			fReader = new NioFileReader(reader.getChannel(), 1024, "utf-8");
			try {
				long time = System.currentTimeMillis(); 
				int i=0;
				while ((line = fReader.readLine()) != null) {
					if (!line.equals("")) {
						super.emit(line);
						i++;
					}
				}
				System.out.println("Path:[" + path + "]\t\tLine:[" + i + "]\t\tms:[" + (System.currentTimeMillis() - time) + "]");
			} catch (Exception e) {
				e.printStackTrace();
			}
			positionMap.put(path.toString(), reader.getFilePointer() > reader
					.length() ? reader.length() : reader.getFilePointer());
		} catch (IOException e) {
			e.printStackTrace();
		}finally{
			if(fReader != null){
				try {
					fReader.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void startRefreshWatchService() {
		refreshWatchService = Executors.newSingleThreadScheduledExecutor();
		refreshWatchService.submit(new RefreshWatchRunnable(refreshWatchService, 5, TimeUnit.SECONDS, this));
	}

	private void startWatchKeyCleanService() {
		watchKeyCleanService = Executors.newSingleThreadScheduledExecutor();
		watchKeyCleanService.submit(new WatchKeyCleanRunnable(
				watchKeyCleanService, 5, TimeUnit.DAYS));
	}

	private void startRefreshPositionService() {
		refreshPositionService = Executors.newSingleThreadScheduledExecutor();
		refreshPositionService.submit(new RefreshPositionRunnable(refreshPositionService, 1, TimeUnit.SECONDS, this));
	}

	private void initialize() {

		clearMaps();
		refreshInit();
	}

	private void refreshInit() {
		refresh(true);
	}

	public void refresh(boolean isFirst) {

		String[] logPaths = initialLogRootsPath(logPathPattern);

		if (logPaths.length == 0) {
			return;
		}
		for (String logPath : logPaths) {
			File logFile = new File(logPath);
			if (!logFile.exists()) {
				continue;
			}
			if (!logFile.isDirectory()) {
				logFile = logFile.getParentFile();
			}
			Path dir = logFile.toPath();
			try {
				if (!dirWatchKeyMap.containsKey(logFile.getAbsolutePath())) {
					WatchKey key = dir.register(watcher,
							StandardWatchEventKinds.ENTRY_MODIFY,
							StandardWatchEventKinds.ENTRY_CREATE);
					dirWatchKeyMap.put(logFile.getAbsolutePath(), key);
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		if (isFirst)
			initialLastPosition(logPaths);
		else
			initialLastPosition(ReadMode.FROM_BEGIN, logPaths);

	}

	private void clearMaps() {
		logFinderMap.clear();
		// for(Entry<Path, RandomAccessFile> entry : readerMap.entrySet()){
		// reader = entry.getValue();
		// if(reader != null){
		// try {
		// reader.close();
		// } catch (IOException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// readerMap.clear();
		WatchKey watchKey;
		for (Entry<String, WatchKey> entry : dirWatchKeyMap.entrySet()) {
			watchKey = entry.getValue();
			if (watchKey != null) {
				try {
					watchKey.cancel();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		dirWatchKeyMap.clear();
	}

	private String[] initialLogRootsPath(String... logPatterns) {
		if (logPatterns.length == 0) {
			throw new RuntimeErrorException(new Error(
					"请检查需要被检索的logpattern配置无误。"));
		}

		Path path;
		String filename;
		Map<Path, Long> map;
		File filePath;
		Set<String> resultSet = new HashSet<String>();
		for (String logPattern : logPatterns) {
			try {
				path = getUsablePath(logPattern);
				if (path == null) {
					System.err.println("logPattern is not usable! pattern: "
							+ logPattern);
					continue;
				}
				if (path.toFile().exists()) {
					EnumSet<FileVisitOption> opts = EnumSet
							.of(FileVisitOption.FOLLOW_LINKS);
					String pattern = (File.separatorChar == '\\') ? logPattern
							.replace("\\", "\\\\") : logPattern;
					LogFinder finder = new LogFinder(pattern);
					Files.walkFileTree(path, opts, Integer.MAX_VALUE, finder);
					if (finder.getMatchedFiles() != null) {
						// resultSet.addAll(finder.getMatchedFiles());
						for (String key : finder.getMatchedFiles()) {
							if (!logFinderMap.containsKey(key)) {
								resultSet.add(key);
								logFinderMap.put(key, finder);
								filePath = new File(key);
								filename = filePath.getName().toString()
										.intern();
								if (filenameMap.containsKey(filename)) {
									map = filenameMap.containsKey(filename) ? filenameMap
											.get(filename)
											: new HashMap<Path, Long>();
									map.put(filePath.getParentFile().toPath(),
											filePath.lastModified());
								} else {
									map = new HashMap<Path, Long>();
									map.put(filePath.getParentFile().toPath(),
											filePath.lastModified());
									filenameMap.put(filename, map);
								}

							}
						}
					}
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		// if(resultSet.size() == 0){
		// throw new RuntimeErrorException(new
		// Error("请检查需要被检索的logpattern配置无误。"));
		// System.out.println("系统根据配置的pattern未匹配到一个文件。。。");
		// }
		if (resultSet.size() > 0)
			System.out.println("初次匹配到的文件有: \t\t\t" + resultSet);
		return resultSet.toArray(new String[resultSet.size()]);
	}

	private Path getUsablePath(String logPattern) {
		File file = new File(logPattern);
		while (file != null && !file.exists()) {
			file = file.getParentFile();
			// System.out.println(file);
		}
		if (file != null) {
			return file.toPath();
		}
		return null;
	}

	private List<Path> resolvePath(Path logFile) {

		String filename = logFile.toString().intern();

		if (!filenameMap.containsKey(filename)) {
			return null;
		}
		long eventTime = System.currentTimeMillis();
		Map<Path, Long> map = filenameMap.get(filename);
		Path result;
		List<Path> list = new ArrayList<Path>(10);
		for (Map.Entry<Path, Long> entry : map.entrySet()) {
			result = entry.getKey().resolve(logFile);
			if (!result.toFile().exists()
					|| eventTime - result.toFile().lastModified() > MAX_DELAY) {
				continue;
			} else {
				list.add(result);
			}
		}
		return list;
	}

	enum ReadMode {
		FROM_BEGIN("from-beginning"), APPEND_ONLY("append-only");
		private String value;

		ReadMode(String value) {
			this.value = value;
		}

		public String getDesc() {
			return this.value;
		}

	}
}