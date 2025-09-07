// src/main/kotlin/br/ars/vnhapi/grpc/config/GrpcExceptionAdvice.kt
package br.ars.vnhapi.grpc.config

import io.grpc.Status
import io.grpc.StatusException
import net.devh.boot.grpc.server.advice.GrpcAdvice
import net.devh.boot.grpc.server.advice.GrpcExceptionHandler

@GrpcAdvice
class GrpcExceptionAdvice {

    @GrpcExceptionHandler(NoSuchElementException::class)
    fun handleNotFound(e: NoSuchElementException): StatusException =
        Status.NOT_FOUND.withDescription(e.message).asException()

    @GrpcExceptionHandler(IllegalArgumentException::class)
    fun handleBadRequest(e: IllegalArgumentException): StatusException =
        Status.INVALID_ARGUMENT.withDescription(e.message).asException()

    @GrpcExceptionHandler(IllegalStateException::class)
    fun handleConflict(e: IllegalStateException): StatusException =
        Status.FAILED_PRECONDITION.withDescription(e.message).asException()

    @GrpcExceptionHandler(Exception::class)
    fun handleGeneric(e: Exception): StatusException =
        Status.UNKNOWN.withDescription(e.message).asException()
}
