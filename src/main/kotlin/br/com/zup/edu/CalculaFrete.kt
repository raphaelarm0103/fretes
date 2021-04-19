package br.com.zup.edu

import io.grpc.ServerBuilder
import io.grpc.stub.StreamObserver

fun main() {

    val server = ServerBuilder
        .forPort(50051)
        .addService(CalculaFreteGrpcServer())
        .build()

    server.start()

    server.awaitTermination()
}

class CalculaFreteGrpcServer: FretesServiceGrpc.FretesServiceImplBase(){
    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        val response = CalculaFreteResponse.newBuilder()
            .setCep(request?.cep)
            .setValor(140.00)
            .build()

        responseObserver?.onNext(response)
        responseObserver?.onCompleted()
    }
}