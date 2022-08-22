package edu.sjsu.cs185c.here;

import edu.sjsu.cs158c.grpcHello.GrpcHello;
import edu.sjsu.cs158c.grpcHello.HelloServiceGrpc;
import io.grpc.ManagedChannelBuilder;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Parameters;

import java.util.concurrent.Callable;

public class Main {

    @Command(name = "hello", subcommands = {CliClient.class, CliServer.class})
    static class TopCommand {
    }
    @Command(name = "helloClient", mixinStandardHelpOptions = true, description = "register attendance for class.")
    static class CliClient implements Callable<Integer> {
        @Parameters(index = "0", description = "server to connect to.")
        String server;

        @Parameters(index = "1", description = "port to connect to.")
        int port;

        @Override
        public Integer call() throws Exception {
            System.out.printf("will contact %s\n", server + ":" + port);
            var channel =
                    ManagedChannelBuilder.forAddress(server, port).usePlaintext().build();
            var stub = HelloServiceGrpc.newBlockingStub(channel);
            var request = GrpcHello.HelloRequest.newBuilder().setName("ben").setCode("init").build();
            System.out.println(stub.hello(request));
            return 0;
        }
    }

    static class HelloImpl extends HelloServiceGrpc.HelloServiceImplBase {
        @Override
        public void hello(GrpcHello.HelloRequest request, StreamObserver<GrpcHello.HelloResponse> responseObserver) {
            System.out.println(request);
            var response = GrpcHello.HelloResponse.newBuilder().setMessage("hello " + request.getName()).build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        }
    }
    @Command(name = "helloServer", mixinStandardHelpOptions = true, description = "register attendance for class.")
    static class CliServer implements Callable<Integer> {
        @Parameters(index = "0", description = "Port to listen on.")
        int port;

        @Override
        public Integer call() throws Exception {
            var server = ServerBuilder.forPort(port).addService(new HelloImpl()).build();
            server.start();
            server.awaitTermination();
            return 0;
        }
    }
    public static void main(String[] args) {
        System.exit(new CommandLine(new TopCommand()).execute(args));
    }
}
