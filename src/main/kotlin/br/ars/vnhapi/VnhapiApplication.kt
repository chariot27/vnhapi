package br.ars.vnhapi

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class VnhapiApplication

fun main(args: Array<String>) {
    runApplication<VnhapiApplication>(*args)
}
