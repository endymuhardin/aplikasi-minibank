var PassbookPrinter = (() => {
  var __create = Object.create;
  var __defProp = Object.defineProperty;
  var __getOwnPropDesc = Object.getOwnPropertyDescriptor;
  var __getOwnPropNames = Object.getOwnPropertyNames;
  var __getProtoOf = Object.getPrototypeOf;
  var __hasOwnProp = Object.prototype.hasOwnProperty;
  var __commonJS = (cb, mod) => function __require() {
    return mod || (0, cb[__getOwnPropNames(cb)[0]])((mod = { exports: {} }).exports, mod), mod.exports;
  };
  var __export = (target, all) => {
    for (var name in all)
      __defProp(target, name, { get: all[name], enumerable: true });
  };
  var __copyProps = (to, from, except, desc) => {
    if (from && typeof from === "object" || typeof from === "function") {
      for (let key of __getOwnPropNames(from))
        if (!__hasOwnProp.call(to, key) && key !== except)
          __defProp(to, key, { get: () => from[key], enumerable: !(desc = __getOwnPropDesc(from, key)) || desc.enumerable });
    }
    return to;
  };
  var __toESM = (mod, isNodeMode, target) => (target = mod != null ? __create(__getProtoOf(mod)) : {}, __copyProps(
    // If the importer is in node compatibility mode or this is not an ESM
    // file that has been converted to a CommonJS file using a Babel-
    // compatible transform (i.e. "__esModule" has not been set), then set
    // "default" to the CommonJS "module.exports" for node compatibility.
    isNodeMode || !mod || !mod.__esModule ? __defProp(target, "default", { value: mod, enumerable: true }) : target,
    mod
  ));
  var __toCommonJS = (mod) => __copyProps(__defProp({}, "__esModule", { value: true }), mod);

  // node_modules/esc-pos-printer/dist/index.js
  var require_dist = __commonJS({
    "node_modules/esc-pos-printer/dist/index.js"(exports) {
      "use strict";
      var __awaiter = exports && exports.__awaiter || function(thisArg, _arguments, P, generator) {
        function adopt(value) {
          return value instanceof P ? value : new P(function(resolve) {
            resolve(value);
          });
        }
        return new (P || (P = Promise))(function(resolve, reject) {
          function fulfilled(value) {
            try {
              step(generator.next(value));
            } catch (e) {
              reject(e);
            }
          }
          function rejected(value) {
            try {
              step(generator["throw"](value));
            } catch (e) {
              reject(e);
            }
          }
          function step(result) {
            result.done ? resolve(result.value) : adopt(result.value).then(fulfilled, rejected);
          }
          step((generator = generator.apply(thisArg, _arguments || [])).next());
        });
      };
      Object.defineProperty(exports, "__esModule", { value: true });
      exports.PrinterModes = exports.PrinterImagesModes = exports.BarcodeModes = exports.QrModes = exports.JustifyModes = void 0;
      var Printer2 = class {
        constructor(printerName, config) {
          var _a, _b;
          this.printerName = printerName !== null && printerName !== void 0 ? printerName : "";
          this.textSpecial = (_a = config === null || config === void 0 ? void 0 : config.textSpecial) !== null && _a !== void 0 ? _a : false;
          this.textAsian = (_b = config === null || config === void 0 ? void 0 : config.textAsian) !== null && _b !== void 0 ? _b : false;
          this.key = config === null || config === void 0 ? void 0 : config.key;
          this.results = [];
        }
        setPrinterName(printerName) {
          this.printerName = printerName;
        }
        /**
         *  add the key to remove watermark
         */
        setKey(key) {
          this.key = key;
        }
        /**
         * for more compatibility with asian characters compatible with some printers
         */
        setPrinterTextAsian(value) {
          this.textAsian = value;
        }
        /**
         * the text rendered of this print instance is going to be printed as unicode
         */
        setPrinterTextSpecial(value) {
          this.textSpecial = value;
        }
        // Method to select print mode based on the mode string
        selectPrintMode(mode) {
          this.results.push({
            type: PrinterActionsTypes.selectPrintMode,
            payload: mode !== null && mode !== void 0 ? mode : void 0
          });
        }
        /**
         * The `justifyCenter` function in TypeScript adds a command to the `results` array to center justify
         * text when printing.
         */
        justify(mode) {
          this.results.push({
            type: PrinterActionsTypes.justify,
            payload: mode
          });
        }
        /**
         * The function `printBase64Image` adds a print action with a base64 image payload to a results array.
         * @param {string} imageBase64 - The `imageBase64` parameter in the `printBase64Image` function is a
         * string that represents an image encoded in Base64 format. This string contains the image data in a
         * format that can be easily transmitted and displayed.
         * @param {PrinterImagesModes} imageMode the imageMode (size)
         */
        printBase64Image(imageBase64, imageMode) {
          this.results.push({
            type: PrinterActionsTypes.printBase64Image,
            payload: imageBase64,
            extraData: imageMode !== null && imageMode !== void 0 ? imageMode : PrinterImagesModes.IMG_DEFAULT
          });
        }
        /**
         * The `text` function in TypeScript adds a text payload to an array of results.
         * @param {string} text - The `text` parameter in the `public text(text: string): void` function is a
         * string type parameter. This function takes a string input and pushes an object with the type
         * `PrinterActionsTypes.text` and the `text` payload into the `results` array.
         */
        text(text) {
          this.results.push({
            type: this.textAsian ? PrinterActionsTypes.textAsian : PrinterActionsTypes.text,
            payload: text
          });
        }
        /**
         *@param {string} value the value to print "ABC"
         *@param {BarcodeModes} mode barcode mode "BARCODE_CODE39" DEFAULT
         */
        barcode(value, mode) {
          this.results.push({
            type: PrinterActionsTypes.barcode,
            payload: value,
            extraData: mode
          });
        }
        /**
         *@param {string} value the value for the qr "ABC"
         *@param {number} size Pixel size to use. Must be 1-16 (default 3)
         *@param {QrModes} model qr model "QR_MODEL_2" DEFAULT
         */
        qrCode(value, size, model) {
          this.results.push({
            type: PrinterActionsTypes.qrCode,
            payload: {
              content: value,
              size: size !== null && size !== void 0 ? size : 3,
              model: model !== null && model !== void 0 ? model : QrModes.QR_MODEL_2
            }
          });
        }
        /**
         * The feed function in TypeScript adds a new item to the results array with a specified value and
         * type.
         * @param {number} value - The `value` parameter in the `feed` method represents the number of units to
         * feed in the printer. This value will be stored in the `payload` property of the object pushed into
         * the `results` array with the type `PrinterActionsTypes.feed`.
         */
        feed(value) {
          this.results.push({
            type: PrinterActionsTypes.feed,
            payload: value !== null && value !== void 0 ? value : void 0
          });
        }
        setEmphasis(value) {
          this.results.push({
            type: PrinterActionsTypes.setEmphasis,
            payload: value
          });
        }
        cut() {
          this.results.push({
            type: PrinterActionsTypes.cut
          });
        }
        close() {
          this.results.push({
            type: PrinterActionsTypes.close
          });
        }
        print() {
          return __awaiter(this, void 0, void 0, function* () {
            const url = "http://localhost:8000/print";
            try {
              const response = yield fetch(url, {
                method: "POST",
                body: JSON.stringify({
                  key: this.key,
                  printer: this.printerName,
                  payload: this.results,
                  textSpecial: this.textSpecial
                })
              });
              if (!response.ok) {
                throw new Error("Failed to print");
              } else {
                return {
                  success: true
                };
              }
            } catch (error) {
              throw new Error("Failed to print");
            }
          });
        }
        getPrinters() {
          return __awaiter(this, void 0, void 0, function* () {
            const url = "http://localhost:8000/printers";
            try {
              const response = yield fetch(url);
              if (!response.ok) {
                throw new Error("Failed getting printer list");
              } else {
                const printersData = yield response.json();
                return printersData;
              }
            } catch (error) {
              throw new Error("Failed getting printer list");
            }
          });
        }
      };
      var PrinterActionsTypes;
      (function(PrinterActionsTypes2) {
        PrinterActionsTypes2["qrCode"] = "qrCode";
        PrinterActionsTypes2["barcode"] = "barcode";
        PrinterActionsTypes2["print"] = "print";
        PrinterActionsTypes2["commands"] = "commands";
        PrinterActionsTypes2["text"] = "text";
        PrinterActionsTypes2["textAsian"] = "textAsian";
        PrinterActionsTypes2["justify"] = "justify";
        PrinterActionsTypes2["printBase64Image"] = "printBase64Image";
        PrinterActionsTypes2["selectPrintMode"] = "selectPrintMode";
        PrinterActionsTypes2["cut"] = "cut";
        PrinterActionsTypes2["setEmphasis"] = "setEmphasis";
        PrinterActionsTypes2["feed"] = "feed";
        PrinterActionsTypes2["pulse"] = "pulse";
        PrinterActionsTypes2["close"] = "close";
      })(PrinterActionsTypes || (PrinterActionsTypes = {}));
      var JustifyModes;
      (function(JustifyModes2) {
        JustifyModes2["justifyCenter"] = "justifyCenter";
        JustifyModes2["justifyLeft"] = "justifyLeft";
        JustifyModes2["justifyRight"] = "justifyRight";
      })(JustifyModes || (exports.JustifyModes = JustifyModes = {}));
      var QrModes;
      (function(QrModes2) {
        QrModes2["QR_MODEL_1"] = "QR_MODEL_1";
        QrModes2["QR_MODEL_2"] = "QR_MODEL_2";
        QrModes2["QR_MICRO"] = "QR_MICRO";
      })(QrModes || (exports.QrModes = QrModes = {}));
      var BarcodeModes;
      (function(BarcodeModes2) {
        BarcodeModes2["BARCODE_UPCA"] = "BARCODE_UPCA";
        BarcodeModes2["BARCODE_UPCE"] = "BARCODE_UPCE";
        BarcodeModes2["BARCODE_JAN13"] = "BARCODE_JAN13";
        BarcodeModes2["BARCODE_JAN8"] = "BARCODE_JAN8";
        BarcodeModes2["BARCODE_CODE39"] = "BARCODE_CODE39";
        BarcodeModes2["BARCODE_ITF"] = "BARCODE_ITF";
        BarcodeModes2["BARCODE_CODABAR"] = "BARCODE_CODABAR";
      })(BarcodeModes || (exports.BarcodeModes = BarcodeModes = {}));
      var PrinterImagesModes;
      (function(PrinterImagesModes2) {
        PrinterImagesModes2["IMG_DEFAULT"] = "IMG_DEFAULT";
        PrinterImagesModes2["IMG_DOUBLE_HEIGHT"] = "IMG_DOUBLE_HEIGHT";
        PrinterImagesModes2["IMG_DOUBLE_WIDTH"] = "IMG_DOUBLE_WIDTH";
      })(PrinterImagesModes || (exports.PrinterImagesModes = PrinterImagesModes = {}));
      var PrinterModes;
      (function(PrinterModes2) {
        PrinterModes2["MODE_DOUBLE_WIDTH"] = "MODE_DOUBLE_WIDTH";
        PrinterModes2["MODE_DOUBLE_HEIGHT"] = "MODE_DOUBLE_HEIGHT";
        PrinterModes2["MODE_EMPHASIZED"] = "MODE_EMPHASIZED";
        PrinterModes2["MODE_FONT_A"] = "MODE_FONT_A";
        PrinterModes2["MODE_FONT_B"] = "MODE_FONT_B";
        PrinterModes2["MODE_UNDERLINE"] = "MODE_UNDERLINE";
      })(PrinterModes || (exports.PrinterModes = PrinterModes = {}));
      exports.default = Printer2;
    }
  });

  // src/escpos-printer.js
  var escpos_printer_exports = {};
  __export(escpos_printer_exports, {
    ESCPOSPrinter: () => ESCPOSPrinter,
    PassbookPrintManager: () => PassbookPrintManager
  });
  var import_esc_pos_printer = __toESM(require_dist());
  var ESCPOSPrinter = class {
    constructor() {
      this.printer = null;
      this.printerName = null;
      this.connected = false;
      this.config = {
        // Column positions (measured from ruler print)
        noCol: 1,
        // Line number
        dateCol: 5,
        // Date DD/MM/YYYY
        sandiCol: 20,
        // Transaction code
        debitCol: 29,
        // Debit amount (right-aligned)
        creditCol: 49,
        // Credit amount (right-aligned)
        balanceCol: 68,
        // Balance (right-aligned)
        tellerCol: 91,
        // Teller name
        // Column widths (adjusted for better fit)
        noWidth: 2,
        // 2 digits for line number
        dateWidth: 10,
        // DD/MM/YYYY = 10 chars
        sandiWidth: 5,
        // Short code
        debitWidth: 16,
        // Amount width
        creditWidth: 16,
        // Amount width
        balanceWidth: 20,
        // Balance width
        tellerWidth: 14,
        // Teller name
        // Lines per page
        linesPerPage: 30,
        // Header offset - number of lines to skip for passbook header
        // Adjust this based on your passbook format
        // Default: 6 lines (for header area)
        headerLines: 6
      };
    }
    /**
     * Check if ESC-POS Printer Manager is available
     */
    async isSupported() {
      try {
        const testPrinter = new import_esc_pos_printer.default();
        if (testPrinter.setServerUrl) {
          testPrinter.setServerUrl("http://localhost:8000");
        }
        const printers = await testPrinter.getPrinters();
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
        const tempPrinter = new import_esc_pos_printer.default();
        if (tempPrinter.setServerUrl) {
          tempPrinter.setServerUrl("http://localhost:8000");
        }
        const printers = await tempPrinter.getPrinters();
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
        if (!printerName) {
          const printers = await this.getPrinters();
          if (printers.length === 0) {
            throw new Error(
              "No printers found. Make sure ESC-POS Printer Manager is running and printer is configured."
            );
          }
          printerName = printers[0];
        }
        this.printer = new import_esc_pos_printer.default();
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
      if (amount === null || amount === void 0) {
        return " ".repeat(width);
      }
      const formatted = new Intl.NumberFormat("id-ID", {
        minimumFractionDigits: 2,
        maximumFractionDigits: 2
      }).format(amount);
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
      const no = this.padString(String(transaction.lineNumber || ""), this.config.noWidth, "right");
      const date = this.formatDate(transaction.transactionDate);
      const sandi = this.padString(transaction.sandiCode || "", this.config.sandiWidth);
      const debit = this.formatAmount(transaction.debit, this.config.debitWidth);
      const credit = this.formatAmount(transaction.credit, this.config.creditWidth);
      const balance = this.formatAmount(transaction.balance, this.config.balanceWidth);
      const teller = this.padString(transaction.tellerName || "", this.config.tellerWidth);
      let line = " ".repeat(100);
      line = this.placeAt(line, this.config.noCol, no);
      line = this.placeAt(line, this.config.dateCol, date);
      line = this.placeAt(line, this.config.sandiCol, sandi);
      line = this.placeAt(line, this.config.debitCol, debit);
      line = this.placeAt(line, this.config.creditCol, credit);
      line = this.placeAt(line, this.config.balanceCol, balance);
      line = this.placeAt(line, this.config.tellerCol, teller);
      return line.trimEnd();
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
        totalLines: transactions.length
      };
      try {
        const actualStartLine = startLine + this.config.headerLines;
        console.log(
          `Print positioning: startLine=${startLine}, headerLines=${this.config.headerLines}, actualStartLine=${actualStartLine}`
        );
        if (actualStartLine > 1) {
          for (let i = 1; i < actualStartLine; i++) {
            this.printer.feed(1);
          }
        }
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
                transaction: tx
              });
            }
          } catch (lineError) {
            console.error(`Failed to print transaction ${tx.id}:`, lineError);
            results.failed.push({ id: tx.id, error: lineError.message });
          }
        }
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
        let tensLine = "";
        for (let i = 0; i < 100; i++) {
          if (i % 10 === 0) {
            tensLine += Math.floor(i / 10);
          } else {
            tensLine += " ";
          }
        }
        this.printer.text(tensLine + "\n");
        let onesLine = "";
        for (let i = 0; i < 100; i++) {
          onesLine += i % 10;
        }
        this.printer.text(onesLine + "\n");
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
        connected: this.connected
      };
    }
  };
  var PassbookPrintManager = class {
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
          supported,
          browser: this.detectBrowser(),
          message: supported ? "ESC-POS Printer Manager is running and printers are available" : "ESC-POS Printer Manager is not running or no printers configured. Please start ESC-POS Printer Manager and configure your printer."
        };
      } catch (error) {
        return {
          supported: false,
          browser: this.detectBrowser(),
          message: "Failed to connect to ESC-POS Printer Manager. Please make sure it is running."
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
          "Content-Type": "application/json"
        },
        body: JSON.stringify({
          accountId,
          status,
          printedTransactionIds: printedIds,
          printerName: printerInfo?.name || "ESC-POS Printer",
          printerPort: printerInfo?.port || "ESC-POS",
          errorMessage
        })
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
        const compat = await this.checkCompatibility();
        if (!compat.supported) {
          throw new Error(compat.message);
        }
        updateStatus("Fetching transaction data...");
        const printData = await this.fetchPrintData(accountId);
        if (!printData.transactions || printData.transactions.length === 0) {
          updateStatus("No transactions to print", "warning");
          return { success: true, message: "No new transactions to print" };
        }
        updateStatus(`Found ${printData.transactions.length} transactions to print`);
        if (!this.isConnected()) {
          updateStatus("Connecting to printer...");
          await this.connectPrinter();
          updateStatus("Printer connected");
        }
        updateStatus("Printing transactions...");
        const startLine = printData.passbook.lastPrintedLine + 1;
        const result = await this.printer.printTransactions(printData.transactions, startLine, onProgress);
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
        method: "POST"
      });
      return await response.json();
    }
  };
  return __toCommonJS(escpos_printer_exports);
})();
