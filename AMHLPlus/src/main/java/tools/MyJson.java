package tools;

import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class MyJson {

//    public void  test(){
//        String fullPath =  "src/test/java/json/test.json";
////例如：fullPath="D:/myroot/test.json"
//
//// 生成json格式文件
//        try {
//            // 保证创建一个新文件
//            File file = new File(fullPath);
//            if (!file.getParentFile().exists())
//            { // 如果父目录不存在，创建父目录
//                file.getParentFile().mkdirs();
//            }
//            if (file.exists()) { // 如果已存在,删除旧文件
//                file.delete();
//            }
//            file.createNewFile();
//
//            //以下创建json格式内容
//            //创建一个json对象，相当于一个容器
//            JSONObject root =new JSONObject();
//            root.put("name","张铁柱");
//            root.put("age",25);
//            //假设身高是double，我们取小数点后一位
//            double height=185.5345;
//            root.put("height",(double)(Math.round(height*10)/10.0));
//            JSONArray array=new JSONArray();
//            JSONObject major1=new JSONObject();
//            major1.put("job1","worker");
//            major1.put("job2","doctor");
//            JSONObject major2=new JSONObject();
//            major2.put("job3","teacher");
//            major2.put("job4","student");
//            array.put( major1);
//            array.put( major2);
//            root.put("major",array);
//            //假设位置x,y都是double型的,现在对他们取整
//            double x=30.0045;
//            double y=30.1123;
//            JSONObject houloc=new JSONObject();
//            houloc.put("x",Math.round(x));
//            houloc.put("y",Math.round(y));
//            root.put("houseLocation",houloc);
//
//
//            // 格式化json字符串
//            String jsonString = formatJson(root.toString());
//
//            // 将格式化后的字符串写入文件
//            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
//            write.write(jsonString);
//            write.flush();
//            write.close();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static void writeJson(String path, JSONObject content) {
        //例如：fullPath="D:/myroot/test.json"
        // 保证创建一个新文件
        File file = new File(path);
        // 生成json格式文件
        try {

            if (!file.getParentFile().exists()) { // 如果父目录不存在，创建父目录
                file.getParentFile().mkdirs();
            }
            if (file.exists()) { // 如果已存在,删除旧文件
                file.delete();
            }
            file.createNewFile();

            // 格式化json字符串
            String jsonString = formatJson(content.toString());

            // 将格式化后的字符串写入文件
            Writer write = new OutputStreamWriter(new FileOutputStream(file), "UTF-8");
            write.write(jsonString);
            write.flush();
            write.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 单位缩进字符串。
     */
    private static String SPACE = "   ";

    /**
     * 返回格式化JSON字符串。
     *
     * @param json 未格式化的JSON字符串。
     * @return 格式化的JSON字符串。
     */
    public static String formatJson(String json) {
        StringBuffer result = new StringBuffer();

        int length = json.length();
        int number = 0;
        char key = 0;

        // 遍历输入字符串。
        for (int i = 0; i < length; i++) {
            // 1、获取当前字符。
            key = json.charAt(i);

            // 2、如果当前字符是前方括号、前花括号做如下处理：
            if ((key == '[') || (key == '{')) {
                // （1）如果前面还有字符，并且字符为“：”，打印：换行和缩进字符字符串。
                if ((i - 1 > 0) && (json.charAt(i - 1) == ':')) {
                    result.append('\n');
                    result.append(indent(number));
                }

                // （2）打印：当前字符。
                result.append(key);

                // （3）前方括号、前花括号，的后面必须换行。打印：换行。
                result.append('\n');

                // （4）每出现一次前方括号、前花括号；缩进次数增加一次。打印：新行缩进。
                number++;
                result.append(indent(number));

                // （5）进行下一次循环。
                continue;
            }

            // 3、如果当前字符是后方括号、后花括号做如下处理：
            if ((key == ']') || (key == '}')) {
                // （1）后方括号、后花括号，的前面必须换行。打印：换行。
                result.append('\n');

                // （2）每出现一次后方括号、后花括号；缩进次数减少一次。打印：缩进。
                number--;
                result.append(indent(number));

                // （3）打印：当前字符。
                result.append(key);

                // （4）如果当前字符后面还有字符，并且字符不为“，”，打印：换行。
                if (((i + 1) < length) && (json.charAt(i + 1) != ',')) {
                    result.append('\n');
                }

                // （5）继续下一次循环。
                continue;
            }

            // 4、如果当前字符是逗号。逗号后面换行，并缩进，不改变缩进次数。
            if ((key == ',')) {
                result.append(key);
                result.append('\n');
                result.append(indent(number));
                continue;
            }

            // 5、打印：当前字符。
            result.append(key);
        }

        return result.toString();
    }

    /**
     * 返回指定次数的缩进字符串。每一次缩进三个空格，即SPACE。
     *
     * @param number 缩进次数。
     * @return 指定缩进次数的字符串。
     */
    private static String indent(int number) {
        StringBuffer result = new StringBuffer();
        for (int i = 0; i < number; i++) {
            result.append(SPACE);
        }
        return result.toString();
    }
}
