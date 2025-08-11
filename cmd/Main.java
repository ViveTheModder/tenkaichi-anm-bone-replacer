package cmd;
//Tenkaichi ANM Tail Replacer by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main 
{
	public static void writeSrcAnms(File[] dirs) throws IOException
	{
		if (dirs[0].equals(dirs[1]))
		{
			System.out.println("Source and destination folders are the exact same!");
			System.exit(1);
		}
		int changedAnms=0;
		File[] srcFiles = dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (srcFiles.length==0 || dstFiles.length==0)
		{
			System.out.println("No source and/or destination ANM files were found!");
			System.exit(2);
		}
		else if (srcFiles.length != dstFiles.length) 
		{
			System.out.println("The number of source & destination ANM files must match!");
			System.exit(3);
		}
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		for (int i=0; i<srcAnms.length; i++)
		{
			srcAnms[i] = new Animation(srcFiles[i]);
			dstAnms[i] = new Animation(dstFiles[i]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation())
			{
				changedAnms++;
				System.out.println("Tail Animation Transfer: "+srcFiles[i].getName()+" (src. ANM "+i+") -> "+dstFiles[i].getName()+" (dest. ANM "+i+")...");
				dstAnms[i].replaceTailBoneContents(srcAnms[i]);
			}
		}
		System.out.print(changedAnms+" ANMs have been changed in ");
	}
	public static void main(String[] args) throws IOException 
	{
		File[] dirs = new File[2];
		Scanner sc = new Scanner(System.in);
		String[] text = {"source","destination"};
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
		sc.close();
		long start = System.currentTimeMillis();
		writeSrcAnms(dirs);
		long end = System.currentTimeMillis();
		System.out.println((end-start)/1000.0+" s");
	}
}