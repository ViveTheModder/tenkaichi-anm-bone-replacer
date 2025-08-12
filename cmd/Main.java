package cmd;
//Tenkaichi ANM Tail Replacer by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main 
{
	public static String[] getBoneNames(File csv) throws IOException
	{
		String[] names = new String[68];
		Scanner sc = new Scanner(csv);
		while (sc.hasNextLine())
		{
			String line = sc.nextLine();
			if (line.equals("id,name")) continue;
			String[] columns = line.split(",");
			int index = Integer.parseInt(columns[0]);
			names[index] = columns[1];
		}
		sc.close();
		return names;
	}
	public static void writeSrcAnms(File[] dirs, int[] boneIds, String[] boneNames) throws IOException
	{
		if (dirs[0].equals(dirs[1]))
		{
			System.out.println("Source and destination folders are the exact same!");
			System.exit(1);
		}
		if (boneIds[1]<boneIds[0])
		{
			System.out.println("End bone ID is smaller than start bone ID!");
			System.exit(2);
		}
		int changedAnms=0;
		File[] srcFiles = dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (srcFiles.length==0 || dstFiles.length==0)
		{
			System.out.println("No source and/or destination ANM files were found!");
			System.exit(3);
		}
		else if (srcFiles.length != dstFiles.length) 
		{
			System.out.println("The number of source & destination ANM files must match!");
			System.exit(4);
		}
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		System.out.print("\nAnimation Transfer (Affected Bones: ");
		for (int i=boneIds[0]; i<=boneIds[1]; i++)
			System.out.print(boneNames[i]+" ");
		System.out.println(")");
		for (int i=0; i<srcAnms.length; i++)
		{
			srcAnms[i] = new Animation(srcFiles[i]);
			dstAnms[i] = new Animation(dstFiles[i]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation())
			{
				changedAnms++;
				System.out.println(srcFiles[i].getName()+" (src. ANM "+i+") -> "+dstFiles[i].getName()+" (dest. ANM "+i+")");
				dstAnms[i].replaceBoneContents(srcAnms[i],boneIds[0],boneIds[1]);
			}
		}
		System.out.print(changedAnms+" ANM files have been changed in ");
	}
	public static void main(String[] args) throws IOException 
	{
		File csv = new File("bone-ids.csv");
		String[] boneNames = getBoneNames(csv);
		int[] boneIds = {1,1};
		File[] dirs = new File[2];
		Scanner sc = new Scanner(System.in);
		String[] text = {"source","destination","start","end"};
		for (int i=0; i<2; i++)
		{
			while (dirs[i]==null)
			{
				System.out.println("Enter a valid path to a "+text[i]+" folder containing ANM files (empty ones will be skipped):");
				String path = sc.nextLine();
				File tmp = new File(path);
				if (tmp.isDirectory()) dirs[i]=tmp;
			}
		}
		for (int i=0; i<2; i++)
		{
			while (boneIds[i]==1)
			{
				System.out.print("Enter a valid "+text[2+i]+" bone ID (different from 1 AND below 56): ");
				String input = sc.nextLine();
				if (input.matches("\\d+"))
				{
					boneIds[i] = Integer.parseInt(input);
					if (boneIds[i]>55) boneIds[i]=55;
				}
			}
		}
		sc.close();
		long start = System.currentTimeMillis();
		writeSrcAnms(dirs,boneIds,boneNames);
		long end = System.currentTimeMillis();
		System.out.println((end-start)/1000.0+" s.");
	}
}