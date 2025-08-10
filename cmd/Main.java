package cmd;
//Tenkaichi ANM Bone Replacer by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class Main 
{
	public static void writeSrcAnms(File[] dirs) throws IOException
	{
		File[] srcFiles = dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (srcFiles.length==0 || dstFiles.length==0)
		{
			System.out.println("No source and/or destination ANM files were found!");
			return;
		}
		else if (srcFiles.length != dstFiles.length) 
		{
			System.out.println("The number of source & destination ANM files must match!");
			return;
		}
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		for (int i=0; i<srcAnms.length; i++)
		{
			srcAnms[i] = new Animation(srcFiles[i]);
			dstAnms[i] = new Animation(dstFiles[i]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation())
			{
				System.out.println("Overwriting tail bone animations of "+dstFiles[i].getName()+"...");
				dstAnms[i].replaceTailBoneContents(srcAnms[i]);
			}
		}
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
				System.out.println("Enter a valid path to a "+text[i]+" folder containing ANM files:");
				String path = sc.nextLine();
				File tmp = new File(path);
				if (tmp.isDirectory()) dirs[i]=tmp;
			}
		}
		sc.close();
		long start = System.currentTimeMillis();
		writeSrcAnms(dirs);
		long end = System.currentTimeMillis();
		System.out.println("Time elapsed: "+(end-start)/1000.0+" s");
	}
}