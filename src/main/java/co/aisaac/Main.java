package co.aisaac;

import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.exif.ExifSubIFDDirectory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;

public class Main {

	public static final DateTimeFormatter NEW_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HHmmss");
	public static final String EXTENSION = ".png";
//	public static final String EXTENSION = ".jpg";
//	public static final String EXTENSION = ".jpeg";
	public static final boolean MOVE = false;

	public static void main(String[] args) {
		new Main().run();
	}

	private void run() {
		// list all files
		File[] files = new File("/users/aaron/photos_backup").listFiles();
		if (files == null) return;

		int i = 0;
		for (File file : files) {
			if (file.isDirectory()) continue;

			if (!file.getName().endsWith(EXTENSION)) continue;

			if (i < 1000)
				System.out.println(i + "\t\t" + file.getName());
			else
				System.out.println(i + "\t" + file.getName());

			// find date created from metadata
			LocalDateTime dateCreated = findDateCreated(file);
			if (dateCreated == null) continue;

			i++;

			move(file, dateCreated);

		}
	}

	// hardcoded move file
	private void move(File file, LocalDateTime dateCreated) {

		String dateString = NEW_DATE_FORMAT.format(dateCreated) + EXTENSION;
		String newName = "/users/aaron/photos_backup/renamed/IMG_" + dateString;

		try {
			System.out.println("Moving file: " + file.getAbsolutePath());
			System.out.println("To:          " + newName);
			if (MOVE)
				Files.move(file.toPath(), new File(newName).toPath());
		} catch (IOException e) {
			System.out.println("Error moving file: " + file.getName());
			e.printStackTrace();
		}
	}

	// finds date created from metadata
	private LocalDateTime findDateCreated(File file) {
		Metadata metadata;
		try {
			metadata = ImageMetadataReader.readMetadata(file);
		} catch (ImageProcessingException | IOException e) {
			System.out.println("Error reading metadata for file: " + file.getName());
			e.printStackTrace();
			return null;
		}

		Directory directory = metadata.getFirstDirectoryOfType(ExifSubIFDDirectory.class);
		if (directory == null) return null;

		Date date = directory.getDate(ExifSubIFDDirectory.TAG_DATETIME_ORIGINAL);
		if (date == null) return null;

		LocalDateTime ldt = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
		return ldt;
	}

	// 2 dates within a day of each other
	private boolean areClose(LocalDateTime ldt, LocalDateTime ldt2) {
		var start = ldt.minusDays(1);
		var end = ldt.plusDays(1);
		return ldt2.isAfter(start) && ldt2.isBefore(end);
	}

	// for extracting a date from a filename as needed
	private LocalDateTime extractDate(String filename) {
		String dateStr = filename.substring(4, 12);
		String yearStr = dateStr.substring(0, 4);
		String monthStr = dateStr.substring(4, 6);
		String dayStr = dateStr.substring(6, 8);

		String timeStr = filename.substring(13, 19);
		String hourStr = timeStr.substring(0, 2);
		String minuteStr = timeStr.substring(2, 4);
		String secondStr = timeStr.substring(4, 6);

		return LocalDateTime.of(
				Integer.parseInt(yearStr),
				Integer.parseInt(monthStr),
				Integer.parseInt(dayStr),
				Integer.parseInt(hourStr),
				Integer.parseInt(minuteStr),
				Integer.parseInt(secondStr)
		);
	}
}