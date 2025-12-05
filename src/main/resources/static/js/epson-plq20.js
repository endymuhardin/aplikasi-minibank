/**
 * Epson PLQ-20 Passbook Printer Driver
 * Uses Web Serial API to communicate directly with the printer
 *
 * ESC/P2 Command Reference for PLQ-20:
 * - ESC @ : Initialize printer
 * - ESC 3 n : Set line spacing to n/180 inch
 * - ESC J n : Advance paper n/180 inch
 * - ESC $ nL nH : Set absolute horizontal position
 * - ESC j n : Reverse feed n/180 inch (for passbook alignment)
 */

class EpsonPLQ20 {
    constructor() {
        this.port = null;
        this.writer = null;
        this.reader = null;
        this.connected = false;

        // Epson vendor ID for USB filter
        this.EPSON_VENDOR_ID = 0x04B8;

        // ESC/P2 Commands
        this.CMD = {
            ESC: 0x1B,
            INIT: [0x1B, 0x40],                    // Initialize printer
            LINE_SPACING_1_6: [0x1B, 0x32],        // 1/6 inch line spacing
            LINE_SPACING_1_8: [0x1B, 0x30],        // 1/8 inch line spacing
            LINE_SPACING_N: [0x1B, 0x33],          // n/180 inch line spacing
            ADVANCE_PAPER: [0x1B, 0x4A],           // Advance paper n/180 inch
            REVERSE_FEED: [0x1B, 0x6A],            // Reverse feed n/180 inch
            ABS_POSITION: [0x1B, 0x24],            // Set absolute horizontal position
            CR: 0x0D,                              // Carriage return
            LF: 0x0A,                              // Line feed
            FF: 0x0C,                              // Form feed
        };

        // Print configuration (can be adjusted for different passbook formats)
        this.config = {
            // Column positions in characters (assuming 10 CPI)
            dateCol: 0,
            descCol: 11,
            debitCol: 32,
            creditCol: 47,
            balanceCol: 62,

            // Column widths
            dateWidth: 10,
            descWidth: 20,
            amountWidth: 14,

            // Line spacing (30 = 1/6 inch for standard line spacing)
            lineSpacing: 30,

            // Characters per inch (10 or 12)
            cpi: 10,
        };
    }

    /**
     * Check if Web Serial API is supported
     */
    isSupported() {
        return 'serial' in navigator;
    }

    /**
     * Request and connect to serial port
     */
    async connect() {
        if (!this.isSupported()) {
            throw new Error('Web Serial API is not supported in this browser. Please use Chrome or Edge.');
        }

        try {
            // Request port with Epson filter
            this.port = await navigator.serial.requestPort({
                filters: [{ usbVendorId: this.EPSON_VENDOR_ID }]
            });

            // Open port with PLQ-20 default settings
            await this.port.open({
                baudRate: 9600,
                dataBits: 8,
                stopBits: 1,
                parity: 'none',
                flowControl: 'none'
            });

            this.writer = this.port.writable.getWriter();
            this.connected = true;

            // Initialize printer
            await this.initialize();

            console.log('Connected to Epson PLQ-20');
            return true;

        } catch (error) {
            console.error('Failed to connect:', error);
            throw error;
        }
    }

    /**
     * Disconnect from printer
     */
    async disconnect() {
        try {
            if (this.writer) {
                this.writer.releaseLock();
                this.writer = null;
            }
            if (this.port) {
                await this.port.close();
                this.port = null;
            }
            this.connected = false;
            console.log('Disconnected from printer');
        } catch (error) {
            console.error('Error disconnecting:', error);
        }
    }

    /**
     * Send raw bytes to printer
     */
    async sendBytes(bytes) {
        if (!this.connected || !this.writer) {
            throw new Error('Printer not connected');
        }
        await this.writer.write(new Uint8Array(bytes));
    }

    /**
     * Send text to printer
     */
    async sendText(text) {
        const encoder = new TextEncoder();
        await this.sendBytes(encoder.encode(text));
    }

    /**
     * Initialize printer
     */
    async initialize() {
        await this.sendBytes(this.CMD.INIT);
        // Set line spacing to 1/6 inch (standard for passbooks)
        await this.sendBytes([...this.CMD.LINE_SPACING_N, this.config.lineSpacing]);
    }

    /**
     * Set absolute horizontal position (in 1/60 inch units)
     */
    async setHorizontalPosition(position) {
        const nL = position & 0xFF;
        const nH = (position >> 8) & 0xFF;
        await this.sendBytes([...this.CMD.ABS_POSITION, nL, nH]);
    }

    /**
     * Advance paper by n lines
     */
    async advanceLines(lines) {
        for (let i = 0; i < lines; i++) {
            await this.sendBytes([this.CMD.LF]);
        }
    }

    /**
     * Advance paper by n/180 inch
     */
    async advancePaper(units) {
        await this.sendBytes([...this.CMD.ADVANCE_PAPER, units]);
    }

    /**
     * Reverse feed by n/180 inch (for alignment)
     */
    async reverseFeed(units) {
        await this.sendBytes([...this.CMD.REVERSE_FEED, units]);
    }

    /**
     * Carriage return (move to beginning of line)
     */
    async carriageReturn() {
        await this.sendBytes([this.CMD.CR]);
    }

    /**
     * Line feed (advance one line)
     */
    async lineFeed() {
        await this.sendBytes([this.CMD.LF]);
    }

    /**
     * Format amount for passbook (right-aligned with thousand separators)
     */
    formatAmount(amount, width = 14) {
        if (amount === null || amount === undefined) {
            return ' '.repeat(width);
        }

        // Format with thousand separators and 2 decimal places
        const formatted = new Intl.NumberFormat('id-ID', {
            minimumFractionDigits: 2,
            maximumFractionDigits: 2
        }).format(amount);

        // Right-align within width
        return formatted.padStart(width);
    }

    /**
     * Format date for passbook
     */
    formatDate(dateString) {
        const date = new Date(dateString);
        const day = String(date.getDate()).padStart(2, '0');
        const month = String(date.getMonth() + 1).padStart(2, '0');
        const year = date.getFullYear();
        return `${day}/${month}/${year}`;
    }

    /**
     * Pad or truncate string to exact width
     */
    padString(str, width, align = 'left') {
        if (!str) str = '';
        str = String(str);

        if (str.length > width) {
            return str.substring(0, width);
        }

        if (align === 'right') {
            return str.padStart(width);
        }
        return str.padEnd(width);
    }

    /**
     * Build a single transaction line for passbook
     */
    buildTransactionLine(transaction) {
        const date = this.formatDate(transaction.transactionDate);
        const desc = this.padString(transaction.description, this.config.descWidth);
        const debit = this.formatAmount(transaction.debit, this.config.amountWidth);
        const credit = this.formatAmount(transaction.credit, this.config.amountWidth);
        const balance = this.formatAmount(transaction.balance, this.config.amountWidth);

        // Build line: DATE | DESCRIPTION | DEBIT | CREDIT | BALANCE
        return `${date} ${desc} ${debit} ${credit} ${balance}`;
    }

    /**
     * Print a single transaction line
     */
    async printTransactionLine(transaction) {
        const line = this.buildTransactionLine(transaction);
        await this.carriageReturn();
        await this.sendText(line);
        await this.lineFeed();
    }

    /**
     * Position to specific line on passbook page
     * @param lineNumber - Line number (1-based)
     * @param startLine - Starting line position on current page
     */
    async positionToLine(lineNumber, startLine = 1) {
        const linesToAdvance = lineNumber - startLine;
        if (linesToAdvance > 0) {
            // Each line is lineSpacing/180 inch
            const units = linesToAdvance * this.config.lineSpacing;
            await this.advancePaper(units);
        }
    }

    /**
     * Print multiple transactions
     * @param transactions - Array of transaction objects
     * @param startLine - Starting line number on passbook (1-based)
     * @param onProgress - Callback for progress updates
     */
    async printTransactions(transactions, startLine = 1, onProgress = null) {
        if (!this.connected) {
            throw new Error('Printer not connected');
        }

        const results = {
            success: true,
            printed: [],
            failed: [],
            totalLines: transactions.length
        };

        try {
            // Initialize printer
            await this.initialize();

            // Position to starting line if not at line 1
            if (startLine > 1) {
                await this.positionToLine(startLine, 1);
            }

            // Print each transaction
            for (let i = 0; i < transactions.length; i++) {
                const tx = transactions[i];

                try {
                    await this.printTransactionLine(tx);
                    results.printed.push(tx.id);

                    if (onProgress) {
                        onProgress({
                            current: i + 1,
                            total: transactions.length,
                            transaction: tx
                        });
                    }

                    // Small delay between lines for printer buffer
                    await this.delay(50);

                } catch (lineError) {
                    console.error(`Failed to print transaction ${tx.id}:`, lineError);
                    results.failed.push({ id: tx.id, error: lineError.message });
                }
            }

            // Final carriage return
            await this.carriageReturn();

            results.success = results.failed.length === 0;

        } catch (error) {
            console.error('Print error:', error);
            results.success = false;
            results.error = error.message;
        }

        return results;
    }

    /**
     * Helper delay function
     */
    delay(ms) {
        return new Promise(resolve => setTimeout(resolve, ms));
    }

    /**
     * Get printer info (for display)
     */
    getPortInfo() {
        if (!this.port) return null;

        const info = this.port.getInfo();
        return {
            vendorId: info.usbVendorId,
            productId: info.usbProductId,
            connected: this.connected
        };
    }
}

/**
 * Passbook Print Manager
 * High-level interface for passbook printing operations
 */
class PassbookPrintManager {
    constructor(apiBaseUrl = '') {
        this.apiBaseUrl = apiBaseUrl;
        this.printer = new EpsonPLQ20();
    }

    /**
     * Check browser compatibility
     */
    checkCompatibility() {
        return {
            supported: this.printer.isSupported(),
            browser: this.detectBrowser(),
            message: this.printer.isSupported()
                ? 'Web Serial API is supported'
                : 'Web Serial API is not supported. Please use Chrome 89+ or Edge 89+'
        };
    }

    /**
     * Detect browser
     */
    detectBrowser() {
        const ua = navigator.userAgent;
        if (ua.includes('Chrome')) return 'Chrome';
        if (ua.includes('Edg')) return 'Edge';
        if (ua.includes('Firefox')) return 'Firefox';
        if (ua.includes('Safari')) return 'Safari';
        return 'Unknown';
    }

    /**
     * Connect to printer
     */
    async connectPrinter() {
        return await this.printer.connect();
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
            throw new Error(error.error || 'Failed to fetch print data');
        }
        return await response.json();
    }

    /**
     * Report print result to server
     */
    async reportPrintResult(accountId, status, printedIds, printerInfo, errorMessage = null) {
        const response = await fetch(`${this.apiBaseUrl}/api/passbook/print-result`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                accountId: accountId,
                status: status,
                printedTransactionIds: printedIds,
                printerName: printerInfo?.name || 'Epson PLQ-20',
                printerPort: printerInfo?.port || 'USB',
                errorMessage: errorMessage
            })
        });

        if (!response.ok) {
            console.error('Failed to report print result');
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
        const updateStatus = (message, type = 'info') => {
            console.log(`[${type}] ${message}`);
            if (onStatus) onStatus({ message, type });
        };

        try {
            // Step 1: Check compatibility
            const compat = this.checkCompatibility();
            if (!compat.supported) {
                throw new Error(compat.message);
            }

            // Step 2: Fetch print data
            updateStatus('Fetching transaction data...');
            const printData = await this.fetchPrintData(accountId);

            if (!printData.transactions || printData.transactions.length === 0) {
                updateStatus('No transactions to print', 'warning');
                return { success: true, message: 'No new transactions to print' };
            }

            updateStatus(`Found ${printData.transactions.length} transactions to print`);

            // Step 3: Connect to printer (if not already connected)
            if (!this.isConnected()) {
                updateStatus('Please select your Epson PLQ-20 printer...');
                await this.connectPrinter();
                updateStatus('Printer connected');
            }

            // Step 4: Print transactions
            updateStatus('Printing transactions...');
            const startLine = printData.passbook.lastPrintedLine + 1;
            const result = await this.printer.printTransactions(
                printData.transactions,
                startLine,
                onProgress
            );

            // Step 5: Report result to server
            const printerInfo = this.printer.getPortInfo();
            const status = result.success ? 'SUCCESS' : (result.printed.length > 0 ? 'PARTIAL' : 'FAILED');

            await this.reportPrintResult(
                accountId,
                status,
                result.printed,
                printerInfo,
                result.error
            );

            if (result.success) {
                updateStatus(`Successfully printed ${result.printed.length} transactions`, 'success');
            } else if (result.printed.length > 0) {
                updateStatus(`Partially printed ${result.printed.length} of ${printData.transactions.length} transactions`, 'warning');
            } else {
                updateStatus('Print failed: ' + result.error, 'error');
            }

            return result;

        } catch (error) {
            updateStatus('Print error: ' + error.message, 'error');
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
            method: 'POST'
        });
        return await response.json();
    }
}

// Export for use in browser
window.EpsonPLQ20 = EpsonPLQ20;
window.PassbookPrintManager = PassbookPrintManager;
