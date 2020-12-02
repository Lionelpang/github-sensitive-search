package com.ai.gss.instance;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
import javax.mail.util.ByteArrayDataSource;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Properties;

/**
 * @author pangms
 * @date 2020/9/24
 */
public class Poster {

    public void send(List<String> receivers, String myAccount, String smtpHost, String uname, String pwd, String content) throws Exception {
        // 1. 创建参数配置, 用于连接邮件服务器的参数配置
        Properties props = new Properties();                    // 参数配置
        props.setProperty("mail.transport.protocol", "smtp");   // 使用的协议（JavaMail规范要求）
        props.setProperty("mail.smtp.host", smtpHost);   // 发件人的邮箱的 SMTP 服务器地址
        props.setProperty("mail.smtp.auth", "true");            // 需要请求认证

        // PS: 某些邮箱服务器要求 SMTP 连接需要使用 SSL 安全认证 (为了提高安全性, 邮箱支持SSL连接, 也可以自己开启),
        //     如果无法连接邮件服务器, 仔细查看控制台打印的 log, 如果有有类似 “连接失败, 要求 SSL 安全连接” 等错误,
        //     取消下面 /* ... */ 之间的注释代码, 开启 SSL 安全连接。
        /*
        // SMTP 服务器的端口 (非 SSL 连接的端口一般默认为 25, 可以不添加, 如果开启了 SSL 连接,
        //                  需要改为对应邮箱的 SMTP 服务器的端口, 具体可查看对应邮箱服务的帮助,
        //                  QQ邮箱的SMTP(SLL)端口为465或587, 其他邮箱自行去查看)
        */

        // 2. 根据配置创建会话对象, 用于和邮件服务器交互
        Session session = Session.getInstance(props);
        // 设置为debug模式, 可以查看详细的发送 log
        // session.setDebug(false);

        // 3. 创建一封邮件
        MimeMessage message = createMimeMessage(session, myAccount, receivers, content);

        // 4. 根据 Session 获取邮件传输对象
        Transport transport = session.getTransport();

        // 5. 使用 邮箱账号 和 密码 连接邮件服务器, 这里认证的邮箱必须与 message 中的发件人邮箱一致, 否则报错
        //
        //    PS_01: 如果连接服务器失败, 都会在控制台输出相应失败原因的log。
        //    仔细查看失败原因, 有些邮箱服务器会返回错误码或查看错误类型的链接,
        //    根据给出的错误类型到对应邮件服务器的帮助网站上查看具体失败原因。
        //
        //    PS_02: 连接失败的原因通常为以下几点, 仔细检查代码:
        //           (1) 邮箱没有开启 SMTP 服务;
        //           (2) 邮箱密码错误, 例如某些邮箱开启了独立密码;
        //           (3) 邮箱服务器要求必须要使用 SSL 安全连接;
        //           (4) 请求过于频繁或其他原因, 被邮件服务器拒绝服务;
        //           (5) 如果以上几点都确定无误, 到邮件服务器网站查找帮助。
        //
        transport.connect(uname, pwd);

        // 6. 发送邮件, 发到所有的收件地址, message.getAllRecipients() 获取到的是在创建邮件对象时添加的所有收件人, 抄送人, 密送人
        transport.sendMessage(message, message.getAllRecipients());

        // 7. 关闭连接
        transport.close();
    }

    /**
     * 创建一封只包含文本的简单邮件
     *
     * @param session     和服务器交互的会话
     * @param sendMail    发件人邮箱
     * @param receiveMail 收件人邮箱
     * @return
     * @throws Exception
     */
    private  MimeMessage createMimeMessage(Session session, String sendMail, List<String> receiveMail, String content) throws Exception {
        // 1. 创建一封邮件
        MimeMessage message = new MimeMessage(session);

        // 2. From: 发件人
        message.setFrom(new InternetAddress(sendMail, sendMail, "UTF-8"));

        // 3. To: 收件人（可以增加多个收件人、抄送、密送）
        // List<InternetAddress> addresses = new ArrayList<>();
        InternetAddress[] addresses = new InternetAddress[receiveMail.size()];
        for (int i=0; i < receiveMail.size(); i++) {
            String rm = receiveMail.get(i);
            addresses[i] = new InternetAddress(rm, rm, "UTF-8");
        }

        // Address[] internetAddressTo = new InternetAddress().parse(receiveMail);
        message.setRecipients(MimeMessage.RecipientType.TO, addresses);

        Calendar calendar = Calendar.getInstance();
        String subject = String.format("Github敏感字扫描%s年%s月%s日",
                String.valueOf(calendar.get(Calendar.YEAR)),
                String.valueOf(calendar.get(Calendar.MONTH) + 1),
                String.valueOf(calendar.get(Calendar.DAY_OF_MONTH))
                );
        // 4. Subject: 邮件主题
        message.setSubject(subject, "UTF-8");

        // 5. Content: 邮件正文（可以使用html标签）
        // content = "您好,\n敏感字搜索规则为如如见所示";
        // message.setContent(content, "text/html;charset=UTF-8");


        /*************** 设置邮件内容: 多功能用户邮件 (related) *******************/
        // 4.1 构建一个多功能邮件块
        MimeMultipart related = new MimeMultipart("mixed");
        // 4.2 构建多功能邮件块内容 = 左侧文本 + 右侧图片资源
        MimeBodyPart mimeBodyPart = new MimeBodyPart();
        MimeBodyPart resource = new MimeBodyPart();

        // 设置具体内容: a.资源(图片)
        // String filePath = MessageDemo2.class.getResource("mm.png").getPath();
        DataSource ds = new ByteArrayDataSource(content, "text/html;charset=UTF-8");// new FileDataSource(new File(filePath));
        //使用到了JAF框架把磁盘上的文件加到resource
        DataHandler handler = new DataHandler(ds);
        resource.setDataHandler(handler);
        resource.setFileName(MimeUtility.encodeText("敏感字规则信息.txt"));
        // resource.setContentID("敏感字规则信息.txt"); // 设置资源名称，给外键引用

        // 设置具体内容: b.文本
        mimeBodyPart.setContent("您好,\n敏感字搜索规则为如如见所示。", "text/html;charset=UTF-8");

        related.addBodyPart(mimeBodyPart);
        related.addBodyPart(resource);

        /******* 4.3 把构建的复杂邮件快，添加到邮件中 ********/
        message.setContent(related);


        // message.setContent(content, "text/html;charset=UTF-8");
        // 6. 设置发件时间
        message.setSentDate(new Date());

        // 7. 保存设置
        message.saveChanges();

        return message;
    }

}
