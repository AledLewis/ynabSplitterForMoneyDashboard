/**
 * MIT License
 *
 * Copyright (c) 2020 Aled Lewis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package dev.aledlewis.ynabsplitterformoneydashboard

import com.github.doyaaaaaken.kotlincsv.dsl.csvReader
import com.github.doyaaaaaken.kotlincsv.dsl.csvWriter
import java.io.File
import java.math.BigDecimal
import java.nio.file.Path
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import kotlin.system.exitProcess


val csvReader = csvReader { this.skipEmptyLine = true }
val csvWriter = csvWriter { }

fun main(args: Array<String>) {
    greeting()
    val filePath = determineFilePath(args)
    val writePath = determineWritePath(args, filePath)
    val file = File(filePath)

    if (!file.exists()) {
        println("File $filePath not found")
        terminate()
    }

    val entries = validateAndParseCsv(file)

    val groupedEntries = entries.groupBy { it.account }

    groupedEntries.forEach { (account, entries) ->
        val fileName = filenameForAccount(account, entries)
        val path = Path.of(writePath, fileName)
        println("Writing ${entries.size} entries to ${path.toAbsolutePath()}")
        writeAccountSpecificCsv(path.toFile(), entries)
    }
    terminate()
}

fun filenameForAccount(account: String, entries: List<MoneyDashBoardCsvEntry>) =
    "${account}_${entries.minBy { it.date }!!.date}_to_${entries.maxBy { it.date }!!.date}.csv"

fun writeAccountSpecificCsv(file: File, entries: List<MoneyDashBoardCsvEntry>){
    if (file.exists()) {
        println("Can't write to already existing file $file, exiting")
        terminate()
    }
    csvWriter.open(file) {
        writeRow(listOf("Date", "Payee", "Memo", "Amount"))
        entries.forEach {
            writeRow(listOf(it.date.toString(), it.description, null, it.amount.toString()))
        }
    }
}

fun determineWritePath(args: Array<String>, filePath:String): String =
    args.getOrNull(1)?.also { println("Writing to: $it") } ?: getWritePathFromCommandLine(filePath)

fun determineFilePath(args: Array<String>): String =
    args.getOrNull(0)?.also { println("Using: $it") } ?: getFileFromCommandLine()


fun getWritePathFromCommandLine(filePath:String): String {
    print("Directory to write to (${Path.of(filePath).parent})")
    return readLine()?.ifEmpty { Path.of(filePath).parent.toString()  } ?: terminate()
}


fun getFileFromCommandLine(): String {
    print("Please enter the file to split")
    return readLine() ?: println("Please enter a file").let { getFileFromCommandLine() }
}

data class MoneyDashBoardCsvEntry(
    val account: String,
    val date: LocalDate,
    val description: String,
    val amount: BigDecimal
)


fun <T, U> Map<T, U>.getOrElseTerminate(key: T): U = this.getOrElse(key) {
    println("Error reading $key from row with $this")
    terminate()
}

fun readRow(csvRow: Map<String, String>): MoneyDashBoardCsvEntry {
    try {
        val account = csvRow.getOrElseTerminate("Account")
        val date = LocalDate.parse(csvRow.getOrElseTerminate("Date"), DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        val description = csvRow.getOrElseTerminate("Description").ifEmpty { csvRow.getOrElseTerminate("Original Description") }
        val amount = csvRow.getOrElseTerminate("Amount").toBigDecimal()
        return MoneyDashBoardCsvEntry(account, date, description, amount)
    } catch (e: Exception) {
        println("Error reading from row: $csvRow , ${e.localizedMessage}")
        terminate()
    }
}

fun validateAndParseCsv(file: File): List<MoneyDashBoardCsvEntry> =
    csvReader.readAllWithHeader(file)
        .map(::readRow)


fun terminate(): Nothing {
    println("Press enter to exit")
    readLine()
    exitProcess(0)
}


fun greeting() {
    listOf(
        "Welcome to the money dashboard extract splitter for YNAB 4 !",
        "I'm going to split up your money dashboard file and write separate files for each account"
    ).also { lines ->
        val maxLength = lines.maxBy { line -> line.length }!!.length
        println("*".repeat(maxLength + 4))
        lines.forEach { println("* $it${" ".repeat(maxLength - it.length)} *") }
        println("*".repeat(maxLength + 4))
    }
}