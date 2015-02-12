package com.mryu.flune.input.tail;

import java.io.File;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Path;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
public class LogFinder implements FileVisitor<Path> {
		
//		private final PathMatcher matcher;
		private final Pattern pattern;
		private List<String> matchedFiles;

		public LogFinder(String pattern) {
			pattern = pattern.replace("*", "[^\\" + File.separator + "]*");
			this.pattern = Pattern.compile(pattern);
//			matcher = FileSystems.getDefault().getPathMatcher("glob" + ":" + pattern);
        }

//		public LogFinder(String pattern, String type) {
//            matcher = FileSystems.getDefault().getPathMatcher(type + ":" + pattern);
//        }
        
		@Override
		public FileVisitResult preVisitDirectory(Path dir,
				BasicFileAttributes attrs) throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFile(Path file, BasicFileAttributes attrs)
				throws IOException {
			Matcher matcher = pattern.matcher(file.toString());
			if(matcher.matches()){
//				System.out.format("%s%n", file);
				if(matchedFiles == null){
					matchedFiles = new ArrayList<>();
				}
				matchedFiles.add(file.toString().intern());
			}
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult visitFileFailed(Path file, IOException exc)
				throws IOException {
			return FileVisitResult.CONTINUE;
		}

		@Override
		public FileVisitResult postVisitDirectory(Path dir, IOException exc)
				throws IOException {
			return FileVisitResult.CONTINUE;
		}

		public List<String> getMatchedFiles() {
			return matchedFiles;
		}
        
		public boolean matches(Path p){
			return pattern.matcher(p.toString()).matches();
		}
	}