package cmd;
//Tenkaichi ANM Bone Replacer by ViveTheJoestar
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import gui.App;

public class Main {
	public static boolean gui = false;
	
	public static String writeSrcAnms(File[] dirs, int[] boneIds, String[] boneNames) throws IOException {
		String error = "";
		if (dirs[0].equals(dirs[1])) {
			error += "Source and destination folders are the exact same!\n";
			if (!gui) {
				System.out.print(error);
				System.exit(1);
			}
		}
		if (boneIds[1] < boneIds[0]) {
			error += "End bone ID is smaller than start bone ID!\n";
			if (!gui) {
				System.out.print(error);
				System.exit(2);
			}
		}
		else {
			if (boneIds[0] == 1) error += "Bone Selection Start must NOT be PRG_RESERVE!\n";
			if (boneIds[1] == 1) error += "Bone Selection End must NOT be PRG_RESERVE!\n";
		}
		int changedAnms = 0;
		boolean dirsAreFiles = dirs[0].isFile() && dirs[1].isFile();
		File[] srcFiles = dirsAreFiles ? new File[1] : dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirsAreFiles ? new File[1] : dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (dirsAreFiles) {
			srcFiles[0] = dirs[0];
			dstFiles[0] = dirs[1];
		}
		if (srcFiles.length == 0 || dstFiles.length == 0) {
			error += "No source and/or destination ANM files were found!\n";
			if (!gui) {
				System.out.print(error);
				System.exit(3);
			}
		}
		else if (srcFiles.length != dstFiles.length) {
			error += "The number of source & destination ANM files must match!\n";
			if (!gui) {
				System.out.print(error);
				System.exit(4);
			}
		}
		if (gui && !error.equals("")) return error;
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		if (!gui) {
			System.out.print("\nAnimation Transfer (Affected Bones: ");
			for (int i = boneIds[0]; i <= boneIds[1]; i++)
				System.out.print(boneNames[i] + " ");
			System.out.println(")");
		}
		int anmTotal = srcAnms.length;
		if (gui) App.bar.setMaximum(anmTotal);
		for (int i = 0; i < srcAnms.length; i++) {
			srcAnms[i] = new Animation(srcFiles[i]);
			dstAnms[i] = new Animation(dstFiles[i]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation()) {
				changedAnms++;
				if (!gui)
					System.out.println(srcAnms[i].getFileName() + " (src. ANM " + i + ") -> " + dstAnms[i].getFileName() + " (dest. ANM " + i + ")");
				else App.bar.setValue(changedAnms);
				dstAnms[i].replaceBoneContents(srcAnms[i], boneIds[0], boneIds[1], boneNames);
				error += dstAnms[i].getAnmError();
			}
			else {
				if (gui) {
					anmTotal--;
					App.bar.setMaximum(anmTotal);
				}
			}
		}
		if (!gui) {
			String success = " ANM files have been changed in ";
			if (changedAnms == 1) success = success.replace("s have", " has");
			System.out.print("SUCCESS: " + changedAnms + success);
		}
		else App.anmCnt = changedAnms;
		return error;
	}
	public static String[] getBoneNames(File csv) throws IOException {
		String[] names = new String[68];
		Scanner sc = new Scanner(csv);
		while (sc.hasNextLine()) {
			String line = sc.nextLine();
			if (line.equals("id,name")) continue;
			String[] columns = line.split(",");
			int index = Integer.parseInt(columns[0]);
			names[index] = columns[1];
		}
		sc.close();
		return names;
	}
	public static void main(String[] args) {
		try {
			if (args.length >= 4) {
				File csv = new File("bone-ids.csv");
				if (csv.isFile()) {
					String[] boneNames = getBoneNames(csv);
					File[] dirs = new File[2];
					for (int i = 0; i < 2; i++)
						dirs[i] = new File(args[i]);
					if (dirs[0].exists() && dirs[1].exists()) {
						int[] boneIds = new int[2];
						if (args[2].matches("\\d+") && args[3].matches("\\d+")) {
							for (int i = 0; i < 2; i++) {
								boneIds[i] = Integer.parseUnsignedInt(args[i + 2]);
								if (boneIds[i] > 67) boneIds[i] = 67;
							}
							long start = System.currentTimeMillis();
							writeSrcAnms(dirs, boneIds, boneNames);
							long end = System.currentTimeMillis();
							System.out.println((end - start) / 1000.0 + " s.");
						}
						else System.out.println("ERROR: Invalid bone IDs!");
					}
					else System.out.println("ERROR: Source and/or destination paths do NOT exist!");
				}
				else System.out.println("ERROR: Required CSV (bone-ids.csv) is missing!");
			}
			else if (args.length > 0)
				System.out.println("USAGE: java -jar \"path/to/src/anm/or/dir\" \"path/to/dst/anm/or/dir\" bone-id-1 bone-id-2");
			else {
				gui = true;
				App.main(args);
			}
		}
		catch (IOException e) {
			String err = e.getClass().getSimpleName() + ": " + e.getMessage();
			System.out.println(err);
		}
	}
}