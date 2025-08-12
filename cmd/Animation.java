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
		if (data.length()<16) return false; //prevents EOFException
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
	private byte[] getUpdatedBoneContents(byte[] contents, short dstAnmFrames)
	{
		byte[] bytes = new byte[2];
		short currKeyframe=0;
		System.arraycopy(contents, 2, bytes, 0, 2);
		short numKeyframes = LittleEndian.getShortFromByteArray(bytes);
		int step = dstAnmFrames/numKeyframes; //average step
		int lastKeyframeIndex = numKeyframes-1, pos=0, posLastKeyframe=0;
		
		for (int i=0; i<numKeyframes; i++)
		{
			if (contents[0]==1) pos = 4+(8*numKeyframes)+(2*i);
			else if (contents[0]==0) pos = 16*(i+1)+(8*i);
			System.arraycopy(LittleEndian.getByteArrayFromShort(LittleEndian.getShort(currKeyframe)), 0, contents, pos, 2);
			currKeyframe+=step;
		}
		if (contents[0]==1) posLastKeyframe = 4+(8*numKeyframes)+(2*lastKeyframeIndex);
		else if (contents[0]==0) pos = 16*(lastKeyframeIndex+1)+(8*lastKeyframeIndex);
		System.arraycopy(LittleEndian.getByteArrayFromShort(LittleEndian.getShort(dstAnmFrames)), 0, contents, posLastKeyframe, 2);
		return contents;
	}
	public RandomAccessFile getData()
	{
		return data;
	}
	public void replaceBoneContents(Animation srcAnm, int startBoneId, int endBoneId) throws IOException
	{
		int startOffset = 6+(2*startBoneId), endOffset = 6+(2*endBoneId);
		int numBones = endBoneId-startBoneId+1;
		data.seek(2); //location of number of frames
		short dstAnmFrames = LittleEndian.getShort(data.readShort());
		int[] srcBoneAddrs = new int[numBones];
		int[] dstBoneAddrs = new int[numBones];
		int[] srcBoneSizes = new int[numBones];
		RandomAccessFile srcData = srcAnm.getData();
		
		srcData.seek(startOffset);
		for (int i=0; i<numBones; i++)
		{
			srcBoneAddrs[i] = LittleEndian.getShort(srcData.readShort());
			srcBoneAddrs[i]*=4;
			srcBoneAddrs[i] = srcBoneAddrs[i]&0xFFFF;
		}
		int srcNextBoneAddr = LittleEndian.getShort(srcData.readShort());
		srcNextBoneAddr*=4;
		srcNextBoneAddr = srcNextBoneAddr&0xFFFF;
		for (int i=1; i<numBones; i++)
			srcBoneSizes[i-1] = srcBoneAddrs[i]-srcBoneAddrs[i-1];
		srcBoneSizes[numBones-1] = srcNextBoneAddr-srcBoneAddrs[numBones-1];
		
		byte[] srcBoneContents = new byte[srcNextBoneAddr-srcBoneAddrs[0]];
		srcData.seek(srcBoneAddrs[0]);
		srcData.read(srcBoneContents);
		int pos=0;
		for (int i=0; i<numBones; i++)
		{
			byte[] newBone = new byte[srcBoneSizes[i]];
			System.arraycopy(srcBoneContents, pos, newBone, 0, newBone.length);
			newBone = getUpdatedBoneContents(newBone, dstAnmFrames);
			System.arraycopy(newBone, 0, srcBoneContents, pos, newBone.length);
			pos+=newBone.length;
		}
		
		data.seek(startOffset);
		for (int i=0; i<numBones; i++)
		{
			dstBoneAddrs[i] = LittleEndian.getShort(data.readShort());
			dstBoneAddrs[i]*=4;
			dstBoneAddrs[i] = dstBoneAddrs[i]&0xFFFF;
		}
		int dstNextBoneAddr = LittleEndian.getShort(data.readShort());
		dstNextBoneAddr*=4;
		dstNextBoneAddr = dstNextBoneAddr&0xFFFF;
		int dstTailContentSize = dstNextBoneAddr-dstBoneAddrs[0];
		int diff = dstTailContentSize-srcBoneContents.length;
		int offsetDiff = (srcBoneAddrs[0]-dstBoneAddrs[0])/4;
		if (diff!=0)
		{
			//fix offsets
			data.seek(endOffset+2);
			for (int offsetCnt=0; offsetCnt<68-(((endOffset+2)-6)/2); offsetCnt++)
			{
				pos = (int)data.getFilePointer();
				short offset = LittleEndian.getShort(data.readShort());
				data.seek(pos);
				if (offset==0) continue;
				offset-=(diff/4);
				if (pos<116) data.writeShort(LittleEndian.getShort(offset));
				else //overwrite offsets of OPTION/EQUIPMENT bones if their data is affected by the replacement
				{
					if (offset>(dstBoneAddrs[numBones-1]/4)) data.writeShort(LittleEndian.getShort(offset));
				}
			}
			data.seek(startOffset);
			for (int offsetCnt=0; offsetCnt<numBones; offsetCnt++)
				data.writeShort(LittleEndian.getShort((short)((srcBoneAddrs[offsetCnt]/4)-offsetDiff)));
			//overwriting process
			data.seek(dstNextBoneAddr);
			byte[] restOfFileContents = new byte[(int)(data.length()-dstNextBoneAddr)];
			data.read(restOfFileContents);
			data.seek(dstBoneAddrs[0]);
			data.write(srcBoneContents);
			data.write(restOfFileContents);
		}
		else
		{
			data.seek(dstBoneAddrs[0]);
			data.write(srcBoneContents);
		}
		data.close();
	}
}