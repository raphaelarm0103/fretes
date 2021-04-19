package br.com.zup.edu

import com.google.protobuf.Any
import io.grpc.Status
import io.grpc.StatusRuntimeException
import io.grpc.protobuf.StatusProto
import io.micronaut.http.HttpStatus
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.exceptions.HttpStatusException
import javax.inject.Inject

@Controller
class CalculadoraDeFretesController(@Inject val gRpcClient: FretesServiceGrpc.FretesServiceBlockingStub) {

    @Get("/api/fretes")
    fun calcula(@QueryValue cep: String): FreteResponse {

        val request = CalculaFreteRequest.newBuilder()
            .setCep(cep)
            .build()

        try {
            val response = gRpcClient.calculaFrete(request)

            return FreteResponse(cep = response.cep, valor = response.valor)
        } catch (e: StatusRuntimeException) {

            val description = e.status.description
            val statusCode = e.status.code

            if(statusCode == Status.Code.INVALID_ARGUMENT){
                throw HttpStatusException(HttpStatus.BAD_REQUEST, e.message)
            }

            if(statusCode == Status.Code.PERMISSION_DENIED){
                val statusProto = StatusProto.fromThrowable(e)
                if(statusProto == null){
                    throw HttpStatusException(HttpStatus.FORBIDDEN, description)
                }

                val anyDetails: Any = statusProto.detailsList.get(0)
                val erroDetails = anyDetails.unpack(ErroDetails::class.java)
                throw HttpStatusException(HttpStatus.FORBIDDEN, "${erroDetails.code}: ${erroDetails.message}")

            }
            // caso contrário
            throw HttpStatusException(HttpStatus.INTERNAL_SERVER_ERROR, e.message)
        }

    }
}

class FreteResponse(val cep: String, val valor: Double) {

}
