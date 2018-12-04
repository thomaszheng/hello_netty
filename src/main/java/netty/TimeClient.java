package netty;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

public class TimeClient {

    private static String host = "127.0.0.1";
    private static int port = 8080;


    public static void main(String[] args) throws Exception {

        EventLoopGroup group = new NioEventLoopGroup();
        try {
            System.out.println(Thread.currentThread());
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class)
             .option(ChannelOption.TCP_NODELAY, true)
             .handler(new ChannelInitializer<SocketChannel>() {
                 @Override
                 public void initChannel(SocketChannel ch) throws Exception {
                     ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {

                         @Override
                         public void channelActive(ChannelHandlerContext ctx) {
                             byte[] req = "QUERY TIME ORDER".getBytes();
                             ByteBuf firstMessage = Unpooled.buffer(req.length);
                             firstMessage.writeBytes(req);
                             ctx.writeAndFlush(firstMessage);
                         }

                         @Override
                         public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
                             ByteBuf buf = (ByteBuf) msg;
                             byte[] req = new byte[buf.readableBytes()];
                             buf.readBytes(req);
                             String body = new String(req, "UTF-8");
                             System.out.println("Now is : " + body);
                         }

                         @Override
                         public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                             // 释放资源
                             ctx.close();
                         }
                     });
                 }
             });

            // 发起异步连接操作
            ChannelFuture f = b.connect(host, port).sync();

            // 当代客户端链路关闭
            f.channel().closeFuture().sync();
        } finally {
            // 优雅退出，释放NIO线程组
            group.shutdownGracefully();
        }
    }
}
