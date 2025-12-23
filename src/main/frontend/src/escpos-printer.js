/**
 * ESC-POS Passbook Printer Driver
 * Uses esc-pos-printer library with ESC-POS Printer Manager
 *
 * Requirements:
 * - ESC-POS Printer Manager application must be running
 * - Printer must be configured as shared printer in Windows
 */

import Printer from "esc-pos-printer";

class ESCPOSPrinter {
	constructor() {
		this.printer = null;
		this.printerName = null;
		this.connected = false;

		// Print configuration - matches physical passbook layout
		// Columns: No | Tanggal | Sandi | Mutasi Debit | Mutasi Kredit | Saldo | Petugas
		this.config = {
			// Column positions (measured from ruler print)
			noCol: 1, // Line number
			dateCol: 5, // Date DD/MM/YYYY
			sandiCol: 20, // Transaction code
			debitCol: 29, // Debit amount (right-aligned)
			creditCol: 49, // Credit amount (right-aligned)
			balanceCol: 68, // Balance (right-aligned)
			tellerCol: 91, // Teller name

			// Column widths (adjusted for better fit)
			noWidth: 2, // 2 digits for line number
			dateWidth: 10, // DD/MM/YYYY = 10 chars
			sandiWidth: 5, // Short code
			debitWidth: 16, // Amount width
			creditWidth: 16, // Amount width
			balanceWidth: 20, // Balance width
			tellerWidth: 14, // Teller name

			// Lines per page
			linesPerPage: 30,

			// Header offset - number of lines to skip for passbook header
			// Adjust this based on your passbook format
			// Default: 6 lines (for header area)
			headerLines: 6,
		};
	}

	/**
	 * Check if ESC-POS Printer Manager is available
	 */
	async isSupported() {
		try {
			const testPrinter = new Printer();
			// Set server URL explicitly (default is http://localhost:8000)
			if (testPrinter.setServerUrl) {
				testPrinter.setServerUrl("http://localhost:8000");
			}
			const printers = await testPrinter.getPrinters();
			// console.log("ESC-POS Printer Manager check - printers found:", printers);
			return printers && printers.length > 0;
		} catch (error) {
			console.error("ESC-POS Printer Manager check failed:", error);
			console.error("Error details:", error.message, error.stack);
			return false;
		}
	}

	/**
	 * Get list of available printers
	 */
	async getPrinters() {
		try {
			const tempPrinter = new Printer();
			// Set server URL explicitly
			if (tempPrinter.setServerUrl) {
				tempPrinter.setServerUrl("http://localhost:8000");
			}
			const printers = await tempPrinter.getPrinters();
			// console.log("Printers retrieved:", printers);
			return printers || [];
		} catch (error) {
			console.error("Failed to get printers:", error);
			console.error("Error details:", error.message);
			throw new Error(
				"Failed to get printer list. Make sure ESC-POS Printer Manager is running on http://localhost:8000"
			);
		}
	}

	/**
	 * Connect to printer by name
	 */
	async connect(printerName = null) {
		try {
			// If no printer name provided, get the first available
			if (!printerName) {
				const printers = await this.getPrinters();
				if (printers.length === 0) {
					throw new Error(
						"No printers found. Make sure ESC-POS Printer Manager is running and printer is configured."
					);
				}
				printerName = printers[0];
			}

			this.printer = new Printer();
			// Set server URL explicitly
			if (this.printer.setServerUrl) {
				this.printer.setServerUrl("http://localhost:8000");
			}
			this.printerName = printerName;
			this.printer.setPrinterName(printerName);
			this.connected = true;

			console.log("Connected to printer:", printerName);
			return true;
		} catch (error) {
			console.error("Failed to connect:", error);
			this.connected = false;
			throw error;
		}
	}

	/**
	 * Disconnect from printer
	 */
	async disconnect() {
		try {
			if (this.printer) {
				this.printer.close();
				this.printer = null;
			}
			this.printerName = null;
			this.connected = false;
			console.log("Disconnected from printer");
		} catch (error) {
			console.error("Error disconnecting:", error);
		}
	}

	/**
	 * Format amount for passbook (right-aligned with thousand separators)
	 */
	formatAmount(amount, width = 14) {
		if (amount === null || amount === undefined) {
			return " ".repeat(width);
		}

		// Format with thousand separators and 2 decimal places
		const formatted = new Intl.NumberFormat("id-ID", {
			minimumFractionDigits: 2,
			maximumFractionDigits: 2,
		}).format(amount);

		// Right-align within width
		return formatted.padStart(width);
	}

	/**
	 * Format date for passbook
	 */
	formatDate(dateString) {
		const date = new Date(dateString);
		const day = String(date.getDate()).padStart(2, "0");
		const month = String(date.getMonth() + 1).padStart(2, "0");
		const year = date.getFullYear();
		return `${day}/${month}/${year}`;
	}

	/**
	 * Pad or truncate string to exact width
	 */
	padString(str, width, align = "left") {
		if (!str) str = "";
		str = String(str);

		if (str.length > width) {
			return str.substring(0, width);
		}

		if (align === "right") {
			return str.padStart(width);
		}
		return str.padEnd(width);
	}

	/**
	 * Build a formatted transaction line
	 * Layout: NO | DATE | SANDI | DEBIT | CREDIT | BALANCE | TELLER
	 */
	buildTransactionLine(transaction) {
		// Format each field
		const no = this.padString(String(transaction.lineNumber || ""), this.config.noWidth, "right");
		const date = this.formatDate(transaction.transactionDate);
		const sandi = this.padString(transaction.sandiCode || "", this.config.sandiWidth);
		const debit = this.formatAmount(transaction.debit, this.config.debitWidth);
		const credit = this.formatAmount(transaction.credit, this.config.creditWidth);
		const balance = this.formatAmount(transaction.balance, this.config.balanceWidth);
		const teller = this.padString(transaction.tellerName || "", this.config.tellerWidth);

		// Build line with absolute positioning
		// Start with empty line of 100 spaces
		let line = " ".repeat(100);

		// Place each field at its absolute position
		line = this.placeAt(line, this.config.noCol, no);
		line = this.placeAt(line, this.config.dateCol, date);
		line = this.placeAt(line, this.config.sandiCol, sandi);
		line = this.placeAt(line, this.config.debitCol, debit);
		line = this.placeAt(line, this.config.creditCol, credit);
		line = this.placeAt(line, this.config.balanceCol, balance);
		line = this.placeAt(line, this.config.tellerCol, teller);

		return line.trimEnd(); // Remove trailing spaces
	}

	/**
	 * Place text at absolute position in a line
	 */
	placeAt(line, position, text) {
		const before = line.substring(0, position);
		const after = line.substring(position + text.length);
		return before + text + after;
	}

	/**
	 * Print multiple transactions
	 * @param transactions - Array of transaction objects
	 * @param startLine - Starting line number on passbook (1-based)
	 * @param onProgress - Callback for progress updates
	 */
	async printTransactions(transactions, startLine = 1, onProgress = null) {
		if (!this.connected || !this.printer) {
			throw new Error("Printer not connected");
		}

		const results = {
			success: true,
			printed: [],
			failed: [],
			totalLines: transactions.length,
		};

		try {
			// Calculate actual line position including header offset
			// For new pages (startLine = 1), we need to skip header lines
			// For continuing pages, startLine already accounts for previous prints,
			// but we still need to add header offset to get absolute position
			const actualStartLine = startLine + this.config.headerLines;

			console.log(
				`Print positioning: startLine=${startLine}, headerLines=${this.config.headerLines}, actualStartLine=${actualStartLine}`
			);

			// Feed to the actual starting position
			if (actualStartLine > 1) {
				for (let i = 1; i < actualStartLine; i++) {
					this.printer.feed(1);
				}
			}

			// Print each transaction
			for (let i = 0; i < transactions.length; i++) {
				const tx = transactions[i];

				try {
					const line = this.buildTransactionLine(tx);
					this.printer.text(line + "\n");
					results.printed.push(tx.id);

					if (onProgress) {
						onProgress({
							current: i + 1,
							total: transactions.length,
							transaction: tx,
						});
					}
				} catch (lineError) {
					console.error(`Failed to print transaction ${tx.id}:`, lineError);
					results.failed.push({ id: tx.id, error: lineError.message });
				}
			}

			// Close and send to printer
			this.printer.close();
			await this.printer.print();

			results.success = results.failed.length === 0;
		} catch (error) {
			console.error("Print error:", error);
			results.success = false;
			results.error = error.message;
		}

		return results;
	}

	/**
	 * Print character ruler for calibration
	 * Prints lines showing character positions (0-9 repeated)
	 */
	async printRuler() {
		if (!this.connected || !this.printer) {
			throw new Error("Printer not connected");
		}

		try {
			// Print ruler line 1: Tens position (0, 10, 20, 30, ...)
			let tensLine = "";
			for (let i = 0; i < 100; i++) {
				if (i % 10 === 0) {
					tensLine += Math.floor(i / 10);
				} else {
					tensLine += " ";
				}
			}
			this.printer.text(tensLine + "\n");

			// Print ruler line 2: Ones position (0123456789 repeated)
			let onesLine = "";
			for (let i = 0; i < 100; i++) {
				onesLine += i % 10;
			}
			this.printer.text(onesLine + "\n");

			// Print ruler line 3: Position markers every 10 chars
			let markerLine = "";
			for (let i = 0; i < 100; i++) {
				if (i % 10 === 0) {
					markerLine += "|";
				} else if (i % 5 === 0) {
					markerLine += "+";
				} else {
					markerLine += ".";
				}
			}
			this.printer.text(markerLine + "\n");

			// Close and send to printer
			this.printer.close();
			await this.printer.print();

			console.log("Ruler printed successfully");
			return { success: true };
		} catch (error) {
			console.error("Ruler print error:", error);
			throw error;
		}
	}

	/**
	 * Helper delay function
	 */
	delay(ms) {
		return new Promise((resolve) => setTimeout(resolve, ms));
	}

	/**
	 * Get printer info (for display)
	 */
	getPortInfo() {
		if (!this.printerName) return null;

		return {
			name: this.printerName,
			port: "ESC-POS",
			connected: this.connected,
		};
	}
}

/**
 * Passbook Print Manager
 * High-level interface for passbook printing operations
 */
class PassbookPrintManager {
	constructor(apiBaseUrl = "") {
		this.apiBaseUrl = apiBaseUrl;
		this.printer = new ESCPOSPrinter();
	}

	/**
	 * Check browser compatibility
	 */
	async checkCompatibility() {
		try {
			const supported = await this.printer.isSupported();
			return {
				supported: supported,
				browser: this.detectBrowser(),
				message: supported
					? "ESC-POS Printer Manager is running and printers are available"
					: "ESC-POS Printer Manager is not running or no printers configured. Please start ESC-POS Printer Manager and configure your printer.",
			};
		} catch (error) {
			return {
				supported: false,
				browser: this.detectBrowser(),
				message: "Failed to connect to ESC-POS Printer Manager. Please make sure it is running.",
			};
		}
	}

	/**
	 * Detect browser
	 */
	detectBrowser() {
		const ua = navigator.userAgent;
		if (ua.includes("Chrome")) return "Chrome";
		if (ua.includes("Edg")) return "Edge";
		if (ua.includes("Firefox")) return "Firefox";
		if (ua.includes("Safari")) return "Safari";
		return "Unknown";
	}

	/**
	 * Get available printers
	 */
	async getAvailablePrinters() {
		return await this.printer.getPrinters();
	}

	/**
	 * Connect to printer
	 */
	async connectPrinter(printerName = null) {
		return await this.printer.connect(printerName);
	}

	/**
	 * Disconnect from printer
	 */
	async disconnectPrinter() {
		return await this.printer.disconnect();
	}

	/**
	 * Check if printer is connected
	 */
	isConnected() {
		return this.printer.connected;
	}

	/**
	 * Fetch print data from server
	 */
	async fetchPrintData(accountId) {
		const response = await fetch(`${this.apiBaseUrl}/api/passbook/${accountId}/print-data`);
		if (!response.ok) {
			const error = await response.json();
			throw new Error(error.error || "Failed to fetch print data");
		}
		return await response.json();
	}

	/**
	 * Report print result to server
	 */
	async reportPrintResult(accountId, status, printedIds, printerInfo, errorMessage = null) {
		const response = await fetch(`${this.apiBaseUrl}/api/passbook/print-result`, {
			method: "POST",
			headers: {
				"Content-Type": "application/json",
			},
			body: JSON.stringify({
				accountId: accountId,
				status: status,
				printedTransactionIds: printedIds,
				printerName: printerInfo?.name || "ESC-POS Printer",
				printerPort: printerInfo?.port || "ESC-POS",
				errorMessage: errorMessage,
			}),
		});

		if (!response.ok) {
			console.error("Failed to report print result");
		}
		return await response.json();
	}

	/**
	 * Main print function
	 * @param accountId - Account UUID
	 * @param onProgress - Progress callback
	 * @param onStatus - Status update callback
	 */
	async printPassbook(accountId, onProgress = null, onStatus = null) {
		const updateStatus = (message, type = "info") => {
			console.log(`[${type}] ${message}`);
			if (onStatus) onStatus({ message, type });
		};

		try {
			// Step 1: Check compatibility
			const compat = await this.checkCompatibility();
			if (!compat.supported) {
				throw new Error(compat.message);
			}

			// Step 2: Fetch print data
			updateStatus("Fetching transaction data...");
			const printData = await this.fetchPrintData(accountId);

			if (!printData.transactions || printData.transactions.length === 0) {
				updateStatus("No transactions to print", "warning");
				return { success: true, message: "No new transactions to print" };
			}

			updateStatus(`Found ${printData.transactions.length} transactions to print`);

			// Step 3: Connect to printer (if not already connected)
			if (!this.isConnected()) {
				updateStatus("Connecting to printer...");
				await this.connectPrinter();
				updateStatus("Printer connected");
			}

			// Step 4: Print transactions
			updateStatus("Printing transactions...");
			const startLine = printData.passbook.lastPrintedLine + 1;
			const result = await this.printer.printTransactions(printData.transactions, startLine, onProgress);

			// Step 5: Report result to server
			const printerInfo = this.printer.getPortInfo();
			const status = result.success ? "SUCCESS" : result.printed.length > 0 ? "PARTIAL" : "FAILED";

			await this.reportPrintResult(accountId, status, result.printed, printerInfo, result.error);

			if (result.success) {
				updateStatus(`Successfully printed ${result.printed.length} transactions`, "success");
			} else if (result.printed.length > 0) {
				updateStatus(
					`Partially printed ${result.printed.length} of ${printData.transactions.length} transactions`,
					"warning"
				);
			} else {
				updateStatus("Print failed: " + result.error, "error");
			}

			return result;
		} catch (error) {
			updateStatus("Print error: " + error.message, "error");
			throw error;
		}
	}

	/**
	 * Get passbook status
	 */
	async getStatus(accountId) {
		const response = await fetch(`${this.apiBaseUrl}/api/passbook/${accountId}/status`);
		return await response.json();
	}

	/**
	 * Advance to next page
	 */
	async advanceToNextPage(accountId) {
		const response = await fetch(`${this.apiBaseUrl}/api/passbook/${accountId}/next-page`, {
			method: "POST",
		});
		return await response.json();
	}
}

// Export for use in browser (will be bundled by esbuild)
export { ESCPOSPrinter, PassbookPrintManager };
