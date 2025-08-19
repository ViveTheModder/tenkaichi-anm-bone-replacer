package cmd;
//Tenkaichi ANM Bone Replacer by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.util.Scanner;
import gui.App;

public class Main 
{
	public static boolean gui=false;
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
	public static String writeSrcAnms(File[] dirs, int[] boneIds, String[] boneNames) throws IOException
	{
		String error="";
		if (dirs[0].equals(dirs[1]))
		{
			error+="Source and destination folders are the exact same!\n";
			if (!gui)
			{
				System.out.print(error);
				System.exit(1);
			}
		}
		if (boneIds[1]<boneIds[0])
		{
			error+="End bone ID is smaller than start bone ID!\n";
			if (!gui)
			{
				System.out.print(error);
				System.exit(2);
			}
		}
		else 
		{
			if (boneIds[0]==1) error+="Bone Selection Start must NOT be PRG_RESERVE!\n";
			if (boneIds[1]==1) error+="Bone Selection End must NOT be PRG_RESERVE!\n";
		}

		int changedAnms=0;
		File[] srcFiles = dirs[0].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		File[] dstFiles = dirs[1].listFiles((dir, name) -> (name.toLowerCase().endsWith(".anm")));
		if (srcFiles.length==0 || dstFiles.length==0)
		{
			error+="No source and/or destination ANM files were found!\n";
			if (!gui)
			{
				System.out.print(error);
				System.exit(3);
			}
		}
		else if (srcFiles.length != dstFiles.length) 
		{
			error+="The number of source & destination ANM files must match!\n";
			if (!gui)
			{
				System.out.print(error);
				System.exit(4);
			}
		}
		if (gui && !error.equals("")) return error;
		
		Animation[] srcAnms = new Animation[srcFiles.length];
		Animation[] dstAnms = new Animation[dstFiles.length];
		if (!gui)
		{
			System.out.print("\nAnimation Transfer (Affected Bones: ");
			for (int i=boneIds[0]; i<=boneIds[1]; i++)
				System.out.print(boneNames[i]+" ");
			System.out.println(")");
		}
		
		int anmTotal = srcAnms.length;
		if (gui) App.bar.setMaximum(anmTotal);
		for (int i=0; i<srcAnms.length; i++)
		{
			srcAnms[i] = new Animation(srcFiles[i]);
			dstAnms[i] = new Animation(dstFiles[i]);
			if (srcAnms[i].isValidAnimation() && dstAnms[i].isValidAnimation())
			{
				changedAnms++;
				if (!gui) System.out.println(srcAnms[i].getFileName()+" (src. ANM "+i+") -> "+dstAnms[i].getFileName()+" (dest. ANM "+i+")");
				else App.bar.setValue(changedAnms);
				dstAnms[i].replaceBoneContents(srcAnms[i],boneIds[0],boneIds[1],boneNames);
				error+=dstAnms[i].getAnmError();
			}
			else
			{
				if (gui)
				{
					anmTotal--;
					App.bar.setMaximum(anmTotal);
				}
			}
		}
		if (!gui) System.out.print(changedAnms+" ANM files have been changed in ");
		else App.anmCnt = changedAnms;
		return error;
	}
	public static void main(String[] args) throws IOException 
	{
		if (args.length>0)
		{
			if (args[0].equals("-c"))
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
						System.out.print("Enter a valid "+text[2+i]+" bone ID (different from 1 AND below 55): ");
						String input = sc.nextLine();
						if (input.matches("\\d+"))
						{
							boneIds[i] = Integer.parseInt(input);
							if (boneIds[i]>67) boneIds[i]=67;
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
		else 
		{
			gui=true;
			App.main(args);
		}
	}
}