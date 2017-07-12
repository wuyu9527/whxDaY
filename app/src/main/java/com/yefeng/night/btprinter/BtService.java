package com.yefeng.night.btprinter;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.os.Environment;
import android.util.Log;

import com.yefeng.night.btprinter.print.GPrinterCommand;
import com.yefeng.night.btprinter.print.PrintPic;
import com.yefeng.night.btprinter.print.PrintQueue;
import com.yefeng.night.btprinter.print.PrintUtil;
import com.yefeng.night.btprinter.util.ZXingUtils;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;

/**
 * Created by yefeng on 6/2/15.
 * github:yefengfreedom
 * <p/>
 * print ticket service
 */
public class BtService extends IntentService {

    public BtService() {
        super("BtService");
    }

    /**
     * Creates an IntentService.  Invoked by your subclass's constructor.
     *
     * @param name Used to name the worker thread, important only for debugging.
     */
    public BtService(String name) {
        super(name);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent == null || intent.getAction() == null) {
            return;
        }
        if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TEST)) {
            printTest();
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT)) {
            print(intent.getByteArrayExtra(PrintUtil.PRINT_EXTRA));
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_TICKET)) {
        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_BITMAP)) {

            printBitmapTest();
//            String url = Environment.getExternalStorageDirectory().getPath();
//            File file = new File(url + "/zgb.pdf");
//            if (file.exists()) {
//                Log.i("whx", "存在");
//                byte[] bytes = getPDFBinary(file);
//                ArrayList<byte[]> bytesList = new ArrayList<byte[]>();
//                bytesList.add(bytes);
//
//                PrintQueue.getQueue(getApplicationContext()).add(bytesList);
//            } else {
//                Log.i("whx", "不存在");
//            }

        } else if (intent.getAction().equals(PrintUtil.ACTION_PRINT_PAINTING)) {
            printPainting();
        }
    }


    public Bitmap getBitmapFromByte(byte[] temp) {
        if (temp != null) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(temp, 0, temp.length);
            return bitmap;
        } else {
            return null;
        }
    }


    private void printTest() {
        try {
            ArrayList<byte[]> bytes = new ArrayList<byte[]>();
            String message = "蓝牙打印测试\n蓝牙打印测试\n蓝牙打印测试\n\n";
            bytes.add(GPrinterCommand.reset);
            bytes.add(message.getBytes("gbk"));
            bytes.add(GPrinterCommand.print);
            bytes.add(GPrinterCommand.print);
            bytes.add(GPrinterCommand.print);
            PrintQueue.getQueue(getApplicationContext()).add(bytes);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private void print(byte[] byteArrayExtra) {
        if (null == byteArrayExtra || byteArrayExtra.length <= 0) {
            return;
        }
        PrintQueue.getQueue(getApplicationContext()).add(byteArrayExtra);
    }

    // 从服务器获得一个输入流(本例是指从服务器获得一个image输入流)
    public static InputStream getInputStream(String urlM) {
        InputStream inputStream = null;
        HttpURLConnection httpURLConnection = null;
        try {
            URL url = new URL(urlM);
            httpURLConnection = (HttpURLConnection) url.openConnection();
// 设置网络连接超时时间
            httpURLConnection.setConnectTimeout(3000);
// 设置应用程序要从网络连接读取数据
            httpURLConnection.setDoInput(true);


            httpURLConnection.setRequestMethod("GET");
            int responseCode = httpURLConnection.getResponseCode();
            if (responseCode == 200) {
// 从服务器返回一个输入流
                inputStream = httpURLConnection.getInputStream();


            }


        } catch (MalformedURLException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
// TODO Auto-generated catch block
            e.printStackTrace();
        }


        return inputStream;


    }

    // String downUrl = "http://www.zgbang.com.cn/download/v2.0/zgbang.apk";
    String downUrl = "https://itunes.apple.com/cn/app/%E6%8E%8C%E6%9F%9C%E5%B8%AE-%E8%80%81%E6%9D%BF%E9%9A%8F%E8%BA%AB%E7%9A%84%E5%BA%97%E9%93%BA%E7%AE%A1%E5%AE%B6/id1119437282?mt=8";

    private void printBitmapTest() {
        BufferedInputStream bis;
        try {
            bis = new BufferedInputStream(getInputStream("https://www.baidu.com/img/bd_logo1.png"));
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }
        //Bitmap bitmap = BitmapFactory.decodeStream(bis);
        Bitmap bitmap1 = ZXingUtils.createQRImage(downUrl, 200, 200);
        Bitmap bitmap2 = ZXingUtils.createQRImage(downUrl, 200, 200);
        Bitmap bitmap4 = ZXingUtils.createQRImage(downUrl, 200, 200);
        Bitmap bitmap3 = ZXingUtils.mixtureBitmap(bitmap1, bitmap2, new PointF(200, 0));
        Bitmap bitmap = ZXingUtils.mixtureBitmap(bitmap4, bitmap3, new PointF(180, 0));
//        Bitmap bitmap = ZXingUtils.createQRImage(downUrl, 250, 250);
        PrintPic printPic = PrintPic.getInstance();
        printPic.init(bitmap);//载入图片
        if (null != bitmap) {//重要判断步骤
            if (bitmap.isRecycled()) {
                bitmap = null;
            } else {
                bitmap.recycle();
                bitmap = null;
            }
        }
        byte[] bytes = printPic.printDraw();//图片字节
        ArrayList<byte[]> printBytes = new ArrayList<byte[]>();
        printBytes.add(GPrinterCommand.reset);
        printBytes.add(GPrinterCommand.print);
        printBytes.add(GPrinterCommand.left);
        printBytes.add(bytes);
        Log.e("BtService", "image bytes size is :" + bytes.length);
        printBytes.add(GPrinterCommand.print);
        //PrintQueue.getQueue(getApplicationContext()).add(bytes);
        PrintQueue.getQueue(getApplicationContext()).add(printBytes);
    }


    /**
     * 将PDF转换成base64编码
     * 1.使用BufferedInputStream和FileInputStream从File指定的文件中读取内容；
     * 2.然后建立写入到ByteArrayOutputStream底层输出流对象的缓冲输出流BufferedOutputStream
     * 3.底层输出流转换成字节数组，然后由BASE64Encoder的对象对流进行编码
     */
    static byte[] getPDFBinary(File file) {
        FileInputStream fin = null;
        BufferedInputStream bin = null;
        ByteArrayOutputStream baos = null;
        BufferedOutputStream bout = null;

        try {
            //建立读取文件的文件输出流
            fin = new FileInputStream(file);
            //在文件输出流上安装节点流（更大效率读取）
            bin = new BufferedInputStream(fin);
            // 创建一个新的 byte 数组输出流，它具有指定大小的缓冲区容量
            baos = new ByteArrayOutputStream();
            //创建一个新的缓冲输出流，以将数据写入指定的底层输出流
            bout = new BufferedOutputStream(baos);
            byte[] buffer = new byte[1024];
            int len = bin.read(buffer);
            while (len != -1) {
                bout.write(buffer, 0, len);
                len = bin.read(buffer);
            }
            //刷新此输出流并强制写出所有缓冲的输出字节，必须这行代码，否则有可能有问题
            bout.flush();
            byte[] bytes = baos.toByteArray();
            //sun公司的API
            //return encoder.encodeBuffer(bytes).trim();
            //apache公司的API
            //return Base64.encodeBase64String(bytes);
            return bytes;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                fin.close();
                bin.close();
                //关闭 ByteArrayOutputStream 无效。此类中的方法在关闭此流后仍可被调用，而不会产生任何 IOException
                //baos.close();
                bout.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    /**
     * 设置绝对位置
     *
     * @param offset 传入位置参数
     */
    public byte[] setOffset(int offset) {
        byte remainder = (byte) (offset % 256);
        byte consult = (byte) (offset / 256);
        byte spaceBytes2[] = {0x1B, 0x24, remainder, consult};
        return spaceBytes2;
    }

    private void printPainting() {
        byte[] bytes = PrintPic.getInstance().printDraw();
        ArrayList<byte[]> printBytes = new ArrayList<byte[]>();
        printBytes.add(GPrinterCommand.reset);
        printBytes.add(GPrinterCommand.print);
        printBytes.add(bytes);
        Log.e("BtService", "image bytes size is :" + bytes.length);
        printBytes.add(GPrinterCommand.print);
        PrintQueue.getQueue(getApplicationContext()).add(bytes);
    }
}