package com.cf.cf685;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.util.Calendar;
import java.util.Random;

import com.cf.wifi.DevGroup;

import android.annotation.SuppressLint;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.media.MediaCodec;
import android.media.MediaFormat;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.SurfaceView;

public class Video
{
    private boolean exitcapture=false;
    ServerSocket server;
    ServerSocket jpgserver;
    Thread   capturethread;
    Thread   jpgcapturethread;
    String   devip="192.168.1.1";
    private  String  MYCAPFile;
    private  String  bmppath;
    private  int     MYCAP=0;
    private  int     MANUALCAP=0;
    private SurfaceView mSurfaceView;
    private Handler handler;
    boolean  codecopen=false;
    boolean  g_isNewDevice=false;

    private final static String MIME_TYPE = "video/avc";
    //	private final static int VIDEO_WIDTH = 1280;
//	private final static int VIDEO_HEIGHT = 720;
    private final static int TIME_INTERNAL = 30;
    private final static int HEAD_OFFSET = 512;
    private MediaCodec mCodec=null;
    Thread  readFileThread;
    private int 	mCount = 0;
    //===========================================
    private  int  videowidth = 1280;
    private  int  videoheight =720;

    private  int    g_oil=0,g_water=0;
    private  int    g_workmode=0;
    private  int    g_videoport=40003;

    private int mTrans=0x0F0F0F0F;
    private boolean bRecord=false;
    private boolean mflag=true;
    private boolean isScale = true;
    private int     stopvideo = 0;

    private int     g_isbm999 = 0;

    private native int InitDecoder();
    private native int UninitDecoder();
    private native int DecoderNal(byte[] in, int insize, byte[] out);
    private native int SleepMS(int ms);
    private native int SearchDevice(byte[] out);
    private native int SetDeviceType(int type);

    private native int Setlight(int who,int zoom);
    private native int Setip(String ip);
    private native int GetJPGFileData(byte[] in, byte[] data,byte[] name);
    private native int DeleteAllJPG();
    private native int GetVideoPort();
    private native DevGroup GetDevGroup();

    static {
        System.loadLibrary("ffmpeg");
        System.loadLibrary("iMVR");
    }

    public Bitmap convertToBitmap(String path, int w, int h)
    {
        BitmapFactory.Options opts = new BitmapFactory.Options();
        opts.inJustDecodeBounds = true;
        opts.inPreferredConfig = Bitmap.Config.ARGB_8888;

        BitmapFactory.decodeFile(path, opts);

        int width = opts.outWidth;
        int height = opts.outHeight;

        Log.e("iMVR","width:"+width+"height:"+height);
        float scaleWidth = 0.f, scaleHeight = 0.f;
        //if (width > w || height > h)
        {
            scaleWidth = ((float) w) / width;
            scaleHeight = ((float) h) / height;
        }
        opts.inJustDecodeBounds = false;
        float scale = Math.max(scaleWidth, scaleHeight);
        opts.inSampleSize = (int)scale;
        WeakReference<Bitmap> weak = new WeakReference<Bitmap>(BitmapFactory.decodeFile(path, opts));
        return Bitmap.createScaledBitmap(weak.get(), w, h, true);
    }

    public Video(SurfaceView  m_SurfaceView,Handler m_handler,String m_path,String m_ip)
    {
        mSurfaceView=m_SurfaceView;
        handler = m_handler;
        bmppath = m_path;
        devip = m_ip;
        g_isNewDevice = false;
        Setip(devip);
    }

    public DevGroup ListAllDev()
    {
        return GetDevGroup();
    }

    public void SetVideoPort(int videoport)
    {
        g_videoport=videoport;
    }

    public int GetDevVideoPort()
    {
        return  GetVideoPort();
    }

    public void SetCapturePath(String m_path)
    {
        bmppath =m_path ;

        File destDir2 = new File(bmppath);
        if (!destDir2.exists()) {
            destDir2.mkdirs();
        }
    }

    public void SetBM999(int is999)
    {
        g_isbm999 = is999;
    }

    public int DeleteAllJPGFile(){ return DeleteAllJPG(); }
    public int SetDevType(int  type){ return SetDeviceType(type); }

    public int SetDevIP(String ip)
    {
        return Setip(ip);
    }


    public void SetNewDevice(boolean newdevice)
    {
        g_isNewDevice = newdevice;
    }

    public void SetWorkMode(int type)
    {
        g_workmode =type;
    }

    public int SwitchLight(int light,int zoom)
    {
        return Setlight(light,zoom);
    }

    public int FindDevice()
    {
        int findevice=0;
        byte []ip = new byte[16];
        findevice = SearchDevice(ip);
        if(findevice==1)
        {
            try {
                devip = new String(ip, "GB2312");
                devip = devip.trim();
                Log.e("iMVR","devip:"+devip);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            Setip(devip);
        }
        return findevice;
    }

    public void InitCodec()
    {
        try {
            OpenDecoder();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        codecopen = true;
    }
    public void ReleaseCodec()
    {
        try {
            CloseDecoder();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        codecopen = false;
    }

    public  void  ManualCapture()
    {
        MYCAP=1;
        MANUALCAP=1;
    }
    public  void  LockScreenAndCapture()
    {
        MYCAP=1;
    }
    public  void  UnLockScreen()
    {
        MYCAP=0;
    }
    public  void  StartKeyThread()
    {
        Log.e("iMVR","StartKeyThread");
        KeyCaptureThead m_keycapture = new  KeyCaptureThead();
        capturethread=new Thread(m_keycapture);
        capturethread.setPriority(Thread.MAX_PRIORITY);
        capturethread.start();

        StartTFJPGThread();
    }

    public  void  StartTFJPGThread()
    {
        Log.e("iMVR","StartTFJPGThread");
        MyJPGCapture m_jpgcapture = new  MyJPGCapture();
        jpgcapturethread=new Thread(m_jpgcapture);
        jpgcapturethread.setPriority(Thread.MAX_PRIORITY);
        jpgcapturethread.start();
    }

    public  void  StopKeyThread()
    {
        exitcapture = true;
        try {
            if(server !=null)
            {
                server.close();
                server = null;
            }

            if(jpgserver !=null)
            {
                jpgserver.close();
                jpgserver = null;
            }
        }
        catch (IOException e){}
    }

    public void StartVideoThread()
    {
        StartVideo();
    }

    public void readytoshow(String filename)
    {
        Message msg=new Message();
        msg.what = 4;
        Bundle bundle=new Bundle();
        bundle.putString("filename",filename);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }
    public void keystatus(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }

    public void showoilandwater(int oil,int water)
    {
        Message msg=new Message();
        msg.what = 5;
        Bundle bundle=new Bundle();
        bundle.putInt("oil",oil);
        bundle.putInt("water",water);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    @SuppressLint("NewApi")
    private void OpenDecoder() throws IOException
    {
        Log.e("iMVR","videowidth:"+videowidth+"videoheight:"+videoheight);

        mCodec = MediaCodec.createDecoderByType(MIME_TYPE);
        MediaFormat mediaFormat = MediaFormat.createVideoFormat(MIME_TYPE,
                videowidth, videoheight);
        mCodec.configure(mediaFormat, mSurfaceView.getHolder().getSurface(),
                null, 0);
        mCodec.start();
    }

    @SuppressLint("NewApi")
    private void CloseDecoder() throws IOException
    {
        if(mCodec!=null)
        {
            mCodec.stop();
            mCodec.release();
            mCodec = null;
        }
    }

    @SuppressLint("NewApi")
    public boolean onFrame(byte[] buf, int offset, int length)
    {
        int wait = 0;
        ByteBuffer[] inputBuffers = mCodec.getInputBuffers();
        int inputBufferIndex = mCodec.dequeueInputBuffer(-1);

        if (inputBufferIndex >= 0) {
            ByteBuffer inputBuffer = inputBuffers[inputBufferIndex];
            inputBuffer.clear();
            inputBuffer.put(buf, offset, length);
            mCodec.queueInputBuffer(inputBufferIndex, 0, length, mCount * 1000000 / 20, 0);
            mCount++;
        } else {
            return false;
        }
        // Get output buffer index
        MediaCodec.BufferInfo bufferInfo = new MediaCodec.BufferInfo();
        int outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, wait);
        while (outputBufferIndex >= 0)
        {
            mCodec.releaseOutputBuffer(outputBufferIndex, true);
            outputBufferIndex = mCodec.dequeueOutputBuffer(bufferInfo, wait);
        }
        return true;
    }
    public String intToIp(int i) {return ((i >> 24 ) & 0xFF ) + "."
            +  ((i >> 16 ) & 0xFF) + "." +  ((i >> 8 ) & 0xFF) + "." +  ( i & 0xFF) ;  }


    public static void saveBitmapToJPG(Bitmap bitmap, File file) throws IOException
    {
        final Random random = new Random();
        int rd1 = random.nextInt();
        int rd2 = random.nextInt();
        Bitmap newBitmap = Bitmap.createBitmap(bitmap.getWidth(),
                bitmap.getHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(newBitmap);
        canvas.drawColor(Color.WHITE);
        //canvas.drawColor(Color.BLACK);
        canvas.drawBitmap(bitmap, 0, 0, null);
        OutputStream stream = new FileOutputStream(file,true);
        newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
        ByteBuffer buffer = ByteBuffer.allocate(6);
        buffer.put((byte)0x45);
        buffer.put((byte)0x7B);
        buffer.put((byte)0x38);
        buffer.put((byte)0xC7);
        buffer.put((byte)rd1);
        buffer.put((byte)rd2);
        stream.write(buffer.array());
        stream.close();
    }

    private void saveBmp(Bitmap bitmap,String filename)
    {
        if (bitmap == null)
            return;
        int nBmpWidth = bitmap.getWidth();
        int nBmpHeight = bitmap.getHeight();

        int bufferSize = nBmpHeight * (nBmpWidth * 3 + nBmpWidth % 4);
        try {
            File file = new File(filename);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream fileos = new FileOutputStream(filename);

            int bfType = 0x4d42;
            long bfSize = 14 + 40 + bufferSize;
            int bfReserved1 = 0;
            int bfReserved2 = 0;
            long bfOffBits = 14 + 40;

            writeWord(fileos, bfType);
            writeDword(fileos, bfSize);
            writeWord(fileos, bfReserved1);
            writeWord(fileos, bfReserved2);
            writeDword(fileos, bfOffBits);

            long biSize = 40L;
            long biWidth = nBmpWidth;
            long biHeight = nBmpHeight;
            int biPlanes = 1;
            int biBitCount = 24;
            long biCompression = 0L;
            long biSizeImage = 0L;
            long biXpelsPerMeter = 0L;
            long biYPelsPerMeter = 0L;
            long biClrUsed = 0L;
            long biClrImportant = 0L;

            writeDword(fileos, biSize);
            writeLong(fileos, biWidth);
            writeLong(fileos, biHeight);
            writeWord(fileos, biPlanes);
            writeWord(fileos, biBitCount);
            writeDword(fileos, biCompression);
            writeDword(fileos, biSizeImage);
            writeLong(fileos, biXpelsPerMeter);
            writeLong(fileos, biYPelsPerMeter);
            writeDword(fileos, biClrUsed);
            writeDword(fileos, biClrImportant);

            byte bmpData[] = new byte[bufferSize];
            int wWidth = (nBmpWidth * 3 + nBmpWidth % 4);
            for (int nCol = 0, nRealCol = nBmpHeight - 1; nCol < nBmpHeight; ++nCol, --nRealCol)
                for (int wRow = 0, wByteIdex = 0; wRow < nBmpWidth; wRow++, wByteIdex += 3) {
                    int clr = bitmap.getPixel(wRow, nCol);
                    bmpData[nRealCol * wWidth + wByteIdex] = (byte) Color.blue(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 1] = (byte) Color.green(clr);
                    bmpData[nRealCol * wWidth + wByteIdex + 2] = (byte) Color.red(clr);
                }

            fileos.write(bmpData);
            fileos.flush();
            fileos.close();

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    protected void writeWord(FileOutputStream stream, int value) throws IOException {
        byte[] b = new byte[2];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        stream.write(b);
    }

    protected void writeDword(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }

    protected void writeLong(FileOutputStream stream, long value) throws IOException {
        byte[] b = new byte[4];
        b[0] = (byte) (value & 0xff);
        b[1] = (byte) (value >> 8 & 0xff);
        b[2] = (byte) (value >> 16 & 0xff);
        b[3] = (byte) (value >> 24 & 0xff);
        stream.write(b);
    }
    public void StartVideo()
    {
        readFileThread = new Thread(readFile);
        readFileThread.start();
    }
    public void StopVideo()
    {
        if(stopvideo ==1)
        {
            mflag = false;
            for(int mm=0;mm<10;mm++)
            {
                if(stopvideo == 0) break;
                try {
                    Thread.sleep(200);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    int MergeBuffer(byte[] NalBuf, int NalBufUsed, byte[] SockBuf, int SockBufUsed, int SockRemain)
    {
        int  i=0;
        byte Temp;
        for(i=0; i<SockRemain; i++)
        {
            Temp  =SockBuf[i+SockBufUsed];
            NalBuf[i+NalBufUsed]=Temp;
            mTrans <<= 8;
            mTrans  |= Temp;
            if(mTrans == 1)
            {
                i++;
                break;
            }
        }
        return i;
    }

    Runnable readFile = new Runnable()
    {

        @Override
        public void run()
        {
            Socket sock = null;
            InputStream is = null;
            OutputStream os = null;
            int nalLen;
            boolean bFirst=true;
            boolean bFindPPS=true;
            int bytesRead=0;
            int NalBufUsed=0;
            int SockBufUsed=0;
            boolean findiframe=false;

            int spslen=14;
            int ppslen=8;
            boolean firstframe=true;
            int decoderframes=0;

            byte []sps = new byte[14];
            byte []pps=new byte[8];

            Bitmap VideoBit;
            byte [] NalBuf = new byte[800000];
            byte [] SockBuf = new byte[8192];

            ByteBuffer buffer;
            byte [] mPixel;

            mPixel = new byte[videowidth*videoheight*2];
            buffer = ByteBuffer.wrap( mPixel );
            VideoBit = Bitmap.createBitmap(videowidth, videoheight, Config.RGB_565);

            buffer.position(0);
            int i = mPixel.length;
            for(i=0; i<mPixel.length; i++)
            {
                mPixel[i]=(byte)0x00;
            }
            Log.e("iMVR","readFile Runnable");
            stopvideo = 1;
            try
            {
                {
                    try
                    {
                        sock = new Socket();
                        SocketAddress socketAddress = new InetSocketAddress(devip, g_videoport);
                        sock.connect(socketAddress,10000);
                    }
                    catch(IOException e)
                    {
                        stopvideo = 0;
                        keystatus(3);
                        return;
                    }
                    is = sock.getInputStream();
                    os = sock.getOutputStream();
                    //	final Random random = new Random();
                    //	int rd = random.nextInt();
                    SockBuf[0] = 0x5f;
                    SockBuf[1] = 0x6f;

                    SockBuf[2] = 0;//1
                    SockBuf[3] = 0;

                    final Calendar c = Calendar.getInstance();
                    int mYear = c.get(Calendar.YEAR);
                    int mMonth = c.get(Calendar.MONTH)+1;
                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                    int mMinute = c.get(Calendar.MINUTE);
                    int mSecond = c.get(Calendar.SECOND);

                    SockBuf[4] = (byte)(mYear-2000);
                    SockBuf[5] = (byte)mMonth;
                    SockBuf[6] = (byte)mDay;
                    SockBuf[7] = (byte)mHour;
                    SockBuf[8] = (byte)mMinute;

                    SockBuf[9] = (byte)mSecond;
                    SockBuf[10] = (byte)11;
                    if(g_isNewDevice)
                        SockBuf[11] = (byte)1; //1:new app ;0 old app
                    else
                        SockBuf[11] = (byte)0;

                    os.write(SockBuf, 0, 12);
                }
            }

            catch(IOException e) { 	Log.e("iMVR","------------------read-----");}

            InitDecoder();
            mflag = true;
            while (mflag == true)
            {
                try
                {
                    bytesRead = is.read(SockBuf, 0, SockBuf.length);
                    //		Log.e("iMVR","--bytesRead:"+bytesRead);
                }
                catch (UnknownHostException e) {
                    e.printStackTrace();
                    Log.e("iMVR","sock err 1.\n");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    Log.e("iMVR","sock err 2.\n");
                    break;
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Log.e("iMVR","sock err 3.\n");
                    break;
                }
                SockBufUsed =0;

                while(bytesRead-SockBufUsed>0)
                {
                    nalLen = MergeBuffer(NalBuf, NalBufUsed, SockBuf, SockBufUsed, bytesRead-SockBufUsed);
                    NalBufUsed += nalLen;
                    SockBufUsed += nalLen;

                    while(mTrans == 1)
                    {
                        mTrans = 0xFFFFFFFF;
                        if(bFirst==true) // the first start flag
                        {
                            bFirst = false;
                        }
                        else  // a complete NAL data, include 0x00000001 trail.
                        {
                            if(bFindPPS==true) // true
                            {
                                if( (NalBuf[4]&0x1F) == 7 )
                                {
                                    bFindPPS = false;
                                }
                                else
                                {
                                    NalBuf[0]=0;
                                    NalBuf[1]=0;
                                    NalBuf[2]=0;
                                    NalBuf[3]=1;
                                    NalBufUsed=4;
                                    break;
                                }
                            }

                            if(firstframe)
                            {
                                DecoderNal(NalBuf, NalBufUsed-4, mPixel);
                                if(NalBuf[4] ==101) firstframe=false;
                            }

                            if(MYCAP ==0)
                            {
                                if(NalBuf[4] ==103) findiframe = true;
                            }
                            else
                                findiframe = false;

                            //	Log.e("iMVR","[0]:"+NalBuf[0]+"[1]:"+NalBuf[1]+"[2]:"+NalBuf[2]+"[3]:"+NalBuf[3]+"[4]:"+NalBuf[4]+"length:"+(NalBufUsed-4));

                            if((MYCAP ==0) && findiframe && (mflag==true))
                                onFrame(NalBuf, 0, NalBufUsed-4);

							 /*
								if(NalBuf[4] ==103)
								{
									System.arraycopy(NalBuf, 0, sps, 0,spslen);
								}
								if(NalBuf[4] ==104)
								{
									System.arraycopy(NalBuf, 0, pps, 0,ppslen);
								}
							*/
                            if(MYCAP ==1)
                            {
                                if(NalBuf[4] == 101)
                                {
                                    if(MANUALCAP==1)
                                    {
                                        MANUALCAP=0;
                                        MYCAP=0;
                                    }
                                    else
                                    {
                                        if(g_workmode == 1)
                                            MYCAP = 2;
                                        else
                                            MYCAP = 0;
                                    }

                                    //  	Log.e("iMVR","[0]:"+sps[0]+"[1]:"+sps[1]+"[2]:"+sps[2]+"[3]:"+sps[3]+"[4]:"+sps[4]+"length:"+spslen);
                                    //   	Log.e("iMVR","[0]:"+pps[0]+"[1]:"+pps[1]+"[2]:"+pps[2]+"[3]:"+pps[3]+"[4]:"+pps[4]+"length:"+ppslen);

                                    //   	DecoderNal(sps, spslen, mPixel);
                                    //	buffer.position(0);
                                    //	VideoBit.copyPixelsFromBuffer(buffer);

                                    //	DecoderNal(pps, ppslen, mPixel);
                                    //	buffer.position(0);
                                    //	VideoBit.copyPixelsFromBuffer(buffer);

                                    DecoderNal(NalBuf, NalBufUsed-4, mPixel);
                                    buffer.position(0);
                                    VideoBit.copyPixelsFromBuffer(buffer);

                                    //	Video.MYCAPFile=getSDPath() +"/"+Long.toString(chan)+"/"+mYear+sMonth+sDay+sHour+sMinute+sSecond+".bmp";
                                    final Calendar c = Calendar.getInstance();
                                    int mYear = c.get(Calendar.YEAR);
                                    int mMonth = c.get(Calendar.MONTH)+1;
                                    int mDay = c.get(Calendar.DAY_OF_MONTH);
                                    int mHour = c.get(Calendar.HOUR_OF_DAY);
                                    int mMinute = c.get(Calendar.MINUTE);
                                    int mSecond = c.get(Calendar.SECOND);   //

                                    //	String sYear = String.valueOf(mYear);
                                    String sMonth = String.valueOf(mMonth);
                                    String sDay = String.valueOf(mDay);
                                    String sHour = String.valueOf(mHour);
                                    String sMinute = String.valueOf(mMinute);
                                    String sSecond = String.valueOf(mSecond);
                                    if(mMonth<10)
                                        sMonth = "0" + String.valueOf(mMonth);
                                    if(mDay<10)
                                        sDay = "0" + String.valueOf(mDay);
                                    if(mHour<10)
                                        sHour = "0" + String.valueOf(mHour);
                                    if(mMinute<10)
                                        sMinute = "0" + String.valueOf(mMinute);
                                    if(mSecond<10)
                                        sSecond = "0" + String.valueOf(mSecond);

                                    //MYCAPFile = bmppath+mYear+sMonth+sDay+sHour+sMinute+sSecond+".bmp";
                                    //saveBmp(VideoBit,MYCAPFile);

                                    MYCAPFile = mYear+sMonth+sDay+sHour+sMinute+sSecond + ".jpg";
                                    //MYCAPFile = System.currentTimeMillis() + ".jpg";

                                    File file = new File(bmppath, MYCAPFile);
                                    try
                                    {
                                        saveBitmapToJPG(VideoBit,file);
                                        Log.e("iMVR","video-->fileName:"+MYCAPFile+"bmppath:"+bmppath);
                                    } catch (IOException e) {
                                        // TODO Auto-generated catch block
                                        e.printStackTrace();
                                    }
                                    readytoshow(MYCAPFile);
                                }
                            }
                        }

                        NalBuf[0]=0;
                        NalBuf[1]=0;
                        NalBuf[2]=0;
                        NalBuf[3]=1;
                        NalBufUsed=4;
                    }
                }
            }
            try{
                if(sock!=null)
                {
                    sock.shutdownInput();
                    sock.shutdownOutput();
                }
                if(os!=null)
                    os.close();
                if(is!=null)
                    is.close();
                if(sock!=null)
                    sock.close();
            }
            catch (IOException e) {
                Log.e("iMVR",e.getMessage());
            }

            UninitDecoder();
            stopvideo = 0;

            Log.e("iMVR","decoder over!\n");
        }
    };

    class KeyCaptureThead implements Runnable
    {
        byte [] SockBuf = new byte[22];
        public void run()
        {
            Log.e("iMVR","Enter KeyCaptureThead");
            exitcapture = false;
            try {
                server = new ServerSocket(40004);
                while(exitcapture == false)
                {
                    Socket client = server.accept();
                    InputStream in=client.getInputStream();
                    int bytesRead = in.read(SockBuf, 0, SockBuf.length);
                    //Log.e("iMVR","--MyCapture bytesRead:"+bytesRead);

                    if((bytesRead == 10) && (SockBuf[8]==1))
                    {
                        g_oil = SockBuf[6];
                        g_water = SockBuf[7];

                        Log.e("iMVR","1--->"+"g_oil:"+g_oil+"g_water:"+g_water);
                        LockScreenAndCapture();
                        keystatus(1); //LOCK
                    }

                    else if((bytesRead == 10) && (SockBuf[8]==0))
                    {
                        Log.e("iMVR","0---->"+"g_workmode:"+g_workmode);
                        if(g_isbm999==1)
                        {
                            Log.e("iMVR","---------------is999---------------");
                            LockScreenAndCapture();
                            if (g_oil > 1) {
                                showoilandwater(g_oil, g_water);
                                Log.e("iMVR", "g_oil:" + g_oil + "g_water:" + g_water);
                            }
                            g_oil = 0;
                            g_water = 0;
                            UnLockScreen();
                            showscalp(20);
                        }
                        else
                        {
                            Log.e("iMVR","---------------is558---------------");
                            LockScreenAndCapture();
                            keystatus(1);//LOCK
                        }
	    					/*
	    					if(g_workmode==1)
	    					{
	    						if(g_oil>1)
		         					showoilandwater(g_oil,g_water);

	    						g_oil = 0;
		    					g_water=0;

		    					UnLockScreen();
	    					}
	    					else
	    					{
	    						LockScreenAndCapture();
		    					keystatus(1);//LOCK
	    					}*/
                    }
	    				/*
	    				if((bytesRead == 10) && (SockBuf[8]==1))
	         			{
	    					LockScreenAndCapture();
	    					keystatus(1); //LOCK
	    				}
	    				else if((bytesRead == 10) && (SockBuf[8]==0))
	    				{
	    					UnLockScreen();
	    					keystatus(2);//unlock
	    				}*/

                    else if((bytesRead == 10) && (SockBuf[8]==5))
                    {
                        shownojpg(10);
                    }
                    else if((bytesRead == 10) && (SockBuf[8]==4))
                    {
                        showdelonejpg(11);
                    }
                    else if((bytesRead == 10) && (SockBuf[8]==6))
                    {
                        showdelalljpg(12);
                    }

                    else if((bytesRead == 10) && (SockBuf[8]==20))
                    {
                        shownopkey(20);
                    }
                    else if((bytesRead == 10) && (SockBuf[8]==21))
                    {
                        shownopkey(21);
                    }
                    else if((bytesRead == 10) && (SockBuf[8]==22))
                    {
                        shownopkey(22);
                    }
                    else if((bytesRead == 10) && (SockBuf[8]==23))
                    {
                        shownopkey(23);
                    }

                    in.close();
                    client.close();
                }
                if(server !=null)
                {
                    server.close();
                    server = null;
                }
            }
            catch (IOException e)
            {
                Log.e("iMVR","Exit MyCapture!");
            }
        }
    }

    class MyJPGCapture implements Runnable
    {

        public void run()
        {
            Log.e("iMVR","MyJPGCapture");
        }
    }

    public void shownopkey(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }
    public void showsdjpg(String filename,boolean newfile)
    {
        Message msg=new Message();
        msg.what =6;
        Bundle bundle=new Bundle();
        bundle.putString("filename",filename);
        bundle.putBoolean("newfile",newfile);
        msg.setData(bundle);
        handler.sendMessage(msg);
    }

    public void showdelalljpg(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }
    public void showdelonejpg(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }
    public void shownojpg(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }

    public void showscalp(int type)
    {
        Message msg = new Message();
        msg.what = type;
        handler.sendMessage(msg);
    }
}
