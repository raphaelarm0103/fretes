package br.com.zup.edu

import com.google.protobuf.Any
import com.google.rpc.Code
import com.google.rpc.StatusOrBuilder
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.grpc.stub.StreamObserver
import org.slf4j.LoggerFactory
import java.lang.Exception
import java.lang.IllegalArgumentException
import java.lang.IllegalStateException
import javax.inject.Singleton
import kotlin.random.Random

@Singleton
class FretesGrpcServer : FretesServiceGrpc.FretesServiceImplBase() {

    private val logger = LoggerFactory.getLogger(FretesGrpcServer::class.java)

    override fun calculaFrete(request: CalculaFreteRequest?, responseObserver: StreamObserver<CalculaFreteResponse>?) {

        logger.info("Calculando frete para request: $request")

        val cep = request?.cep
        if (cep == null || cep.isBlank()) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep deve ser informado")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        if (!cep!!.matches("[0-9]{5}-[0-9]{3}".toRegex())) {
            val e = Status.INVALID_ARGUMENT
                .withDescription("cep inválido")
                .augmentDescription("formato esperado deve ser 99999-999")
                .asRuntimeException()
            responseObserver?.onError(e)
        }

        // simulando verificação de segurança
        if(cep.endsWith("333")){

          val statusProto =  com.google.rpc.Status.newBuilder()
                .setCode(Code.PERMISSION_DENIED.number)
                .setMessage("usuario não pode acessar esse recurso")
              .addDetails(Any.pack(ErroDetails.newBuilder()
                            .setCode(401)
                              .setMessage("token expirado")
                              .build()))

                .build()
            val e = StatusProto.toStatusRuntimeException(statusProto)
            responseObserver?.onError(e)
        }

        var valor = 0.0
        try {
            valor = Random.nextDouble(from = 0.0, until = 140.0)
            if (valor > 100.0) {
                throw IllegalStateException("Erro inesperado")
            }
        } catch (e: Exception) {
            responseObserver?.onError(
                Status.INTERNAL
                    .withDescription(e.message)
                    .withCause(e) // é anexado ao grpc, mas não é enviado ao Client
                    .asRuntimeException()
            )
        }


        val response = CalculaFreteResponse.newBuilder()
            .setCep(request!!.cep)
            .setValor(valor)
            .build()

        logger.info("Frete Calculado: $response")

        responseObserver!!.onNext(response)
        responseObserver.onCompleted()

    }
}