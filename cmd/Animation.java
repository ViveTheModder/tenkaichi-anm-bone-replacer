package cmd;
//Tenkaichi Animation Class by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Animation 
{
	private RandomAccessFile data;
	public Animation(File f)
	{
		try 
		{
			data = new RandomAccessFile(f,"rw");
		} 
		catch (IOException e) 
		{
			e.printStackTrace();
		}
	}
	public boolean isValidAnimation() throws IOException
	{
		boolean headerCheck=false;
		data.seek(0);
		byte header1 = data.readByte();
		if (header1==0x61) headerCheck=true;
		short header2,header3;
		data.seek(4);
		header2 = data.readShort();
		data.seek(8);
		header3 = data.readShort();
		if (header2==0 && header3==0 && headerCheck) return true;
		return false;
	}
	private byte[] getUpdatedTailBoneContents(byte[] contents, short dstAnmFrames)
	{
		if (contents[0]==1) //check if bone only has rotation data (not translation and rotation)
		{
			byte[] bytes = new byte[2];
			short currKeyframe=0;
			System.arraycopy(contents, 2, bytes, 0, 2);
			short numKeyframes = LittleEndian.getShortFromByteArray(bytes);
			int step = dstAnmFrames/numKeyframes; //average step
			int lastKeyframeIndex = numKeyframes-1;
			for (int i=0; i<numKeyframes; i++)
			{
				int pos = 4+(8*numKeyframes)+(2*i);
				System.arraycopy(LittleEndian.getByteArrayFromShort(LittleEndian.getShort(currKeyframe)), 0, contents, pos, 2);
				currKeyframe+=step;
			}
			int posLastKeyframe = 4+(8*numKeyframes)+(2*lastKeyframeIndex);
			System.arraycopy(LittleEndian.getByteArrayFromShort(LittleEndian.getShort(dstAnmFrames)), 0, contents, posLastKeyframe, 2);
		}
		return contents;
	}
	public RandomAccessFile getData()
	{
		return data;
	}
	public void replaceTailBoneContents(Animation srcAnm) throws IOException
	{
		data.seek(2); //location of number of frames
		short dstAnmFrames = LittleEndian.getShort(data.readShort());
		short[] srcTailBoneAddrs = new short[4];
		short[] dstTailBoneAddrs = new short[4];
		int[] srcTailBoneSizes = new int[4];
		RandomAccessFile srcData = srcAnm.getData();
		
		srcData.seek(14); //location of TAIL1 offset
		for (int i=0; i<4; i++)
		{
			srcTailBoneAddrs[i] = LittleEndian.getShort(srcData.readShort());
			srcTailBoneAddrs[i]*=4;
		}
		short srcRightHipAddr = LittleEndian.getShort(srcData.readShort());
		srcRightHipAddr*=4;
		for (int i=1; i<4; i++)
			srcTailBoneSizes[i-1] = srcTailBoneAddrs[i]-srcTailBoneAddrs[i-1];
		srcTailBoneSizes[3] = srcRightHipAddr-srcTailBoneAddrs[3];
		
		byte[] srcTailBoneContents = new byte[srcRightHipAddr-srcTailBoneAddrs[0]];
		srcData.seek(srcTailBoneAddrs[0]);
		srcData.read(srcTailBoneContents);
		int pos=0;
		for (int i=0; i<4; i++)
		{
			byte[] newTailBone = new byte[srcTailBoneSizes[i]];
			System.arraycopy(srcTailBoneContents, pos, newTailBone, 0, newTailBone.length);
			newTailBone = getUpdatedTailBoneContents(newTailBone, dstAnmFrames);
			System.arraycopy(newTailBone, 0, srcTailBoneContents, pos, newTailBone.length);
			pos+=newTailBone.length;
		}
		
		data.seek(14); //location of TAIL1 offset
		for (int i=0; i<4; i++)
		{
			dstTailBoneAddrs[i] = LittleEndian.getShort(data.readShort());
			dstTailBoneAddrs[i]*=4;
		}
		short dstRightHipAddr = LittleEndian.getShort(data.readShort());
		dstRightHipAddr*=4;
		int dstTailContentSize = dstRightHipAddr-dstTailBoneAddrs[0];
		int diff = dstTailContentSize-srcTailBoneContents.length;
		int offsetDiff = (srcTailBoneAddrs[0]-dstTailBoneAddrs[0])/4;
		if (diff!=0)
		{
			//fix offsets
			data.seek(22); //location of HIP_R offset
			for (int offsetCnt=0; offsetCnt<53; offsetCnt++)
			{
				short offset = LittleEndian.getShort(data.readShort());
				if (offset==0) continue;
				data.seek(data.getFilePointer()-2);
				offset-=(diff/4);
				data.writeShort(LittleEndian.getShort(offset));
			}
			data.seek(14); //location of TAIL1 offset
			for (int offsetCnt=0; offsetCnt<4; offsetCnt++)
				data.writeShort(LittleEndian.getShort((short)((srcTailBoneAddrs[offsetCnt]/4)-offsetDiff)));
			//overwriting process
			data.seek(dstRightHipAddr);
			byte[] restOfFileContents = new byte[(int)(data.length()-dstRightHipAddr)];
			data.read(restOfFileContents);
			data.seek(dstTailBoneAddrs[0]);
			data.write(srcTailBoneContents);
			data.write(restOfFileContents);
		}
		else
		{
			data.seek(dstTailBoneAddrs[0]);
			data.write(srcTailBoneContents);
		}
		data.close();
	}
}