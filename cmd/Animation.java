package cmd;
//Tenkaichi Animation Class by ViveTheModder
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class Animation 
{
	private String error;
	private String fileName;
	private RandomAccessFile data;
	public Animation(File f)
	{
		try 
		{
			data = new RandomAccessFile(f,"rw");
			error = "";
			fileName = f.getName(); 
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
	public RandomAccessFile getData()
	{
		return data;
	}
	public String getAnmError()
	{
		return error;
	}
	public String getFileName()
	{
		return fileName;
	}
	public void replaceBoneContents(Animation srcAnm, int startBoneId, int endBoneId, String[] boneNames) throws IOException
	{
		data.seek(2); //location of number of frames
		short dstAnmFrames = LittleEndian.getShort(data.readShort());
		RandomAccessFile srcData = srcAnm.getData();

		for (int boneId=startBoneId; boneId<=endBoneId; boneId++)
		{
			int dstAddr, srcAddr, offset = 6+(2*boneId), size=0;
			srcData.seek(offset);
			srcAddr = LittleEndian.getShort(srcData.readShort());
			if (srcAddr==0) 
			{
				setError(boneNames[boneId]+" contents are NOT present in "+srcAnm.getFileName()+" (src. ANM)!\n");
				continue;
			}
			srcAddr = getAddress(srcAddr);
			srcData.seek(srcAddr);
			short boneType = LittleEndian.getShort(srcData.readShort());
			short numKeyframes = LittleEndian.getShort(srcData.readShort());
			size = getBoneContentsSize(boneType,numKeyframes);
			
			srcData.seek(srcAddr);
			byte[] srcBoneContents = new byte[size];
			srcData.read(srcBoneContents);
			srcBoneContents = getUpdatedBoneContents(srcBoneContents, dstAnmFrames);
			
			data.seek(offset);
			dstAddr = LittleEndian.getShort(data.readShort());
			if (dstAddr==0) 
			{
				setError(boneNames[boneId]+" contents are NOT present in "+this.getFileName()+" (dst. ANM)!\n");
				continue;
			}
			dstAddr = getAddress(dstAddr);
			data.seek(dstAddr);
			boneType = LittleEndian.getShort(data.readShort());
			numKeyframes = LittleEndian.getShort(data.readShort());
			size = getBoneContentsSize(boneType,numKeyframes);
			
			int nextAddr = dstAddr+size;
			int diff = size-srcBoneContents.length;
			if (diff!=0)
			{
				//fix index
				for (int pos=6; pos<144; pos+=2)
				{
					data.seek(pos);
					short newOffset = LittleEndian.getShort(data.readShort());
					int newAddr = (newOffset*4)&0xFFFF;
					if (newAddr>dstAddr)
					{
						newOffset-=(diff/4);
						data.seek(pos);
						data.writeShort(LittleEndian.getShort(newOffset));
					}
				}
				//overwriting process
				data.seek(nextAddr);
				byte[] restOfFileContents = new byte[(int)(data.length()-nextAddr)];
				data.read(restOfFileContents);
				data.seek(dstAddr);
				data.write(srcBoneContents);
				data.write(restOfFileContents);
			}
		}
		int newFileSize = (int)data.length();
		if (newFileSize%16!=0) newFileSize = newFileSize+16-(newFileSize%16);
		data.setLength(newFileSize);
		data.close();
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
		else if (contents[0]==0) posLastKeyframe = 16*(lastKeyframeIndex+1)+(8*lastKeyframeIndex);
		System.arraycopy(LittleEndian.getByteArrayFromShort(LittleEndian.getShort(dstAnmFrames)), 0, contents, posLastKeyframe, 2);
		return contents;
	}
	private int getAddress(int addr)
	{
		addr*=4;
		return addr&0xFFFF;
	}
	private int getBoneContentsSize(short boneType, short numKeyframes)
	{
		int size=0;
		if (boneType==1) size=(10*numKeyframes)+4;
		else if (boneType==0) size=(24*numKeyframes+4);
		if (size!=0 && size%4!=0) size=size+4-(size%4);
		return size;
	}
	private void setError(String error)
	{
		this.error += error;
	}
}