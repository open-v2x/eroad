let CryptJS = require("crypto-js");



export const AESUtil = {
    /**
     * AES 加密
     * @param _content 待加密内容
     * @param _key aesKey,
     * @param _iv 初始化向量
     * @return 返回经 BASE64 处理之后的密文
     */
    encrypt: function (_content, _key, _iv) {
        // 先以 UTF-8 编码解码参数 返回 any 类型
        let content = CryptJS.enc.Utf8.parse(_content);
        let aesKey = CryptJS.enc.Utf8.parse(_key);
        let iv = CryptJS.enc.Utf8.parse(_iv);

        // 加密
        let encrypted = CryptJS.AES.encrypt(content, aesKey, {
            iv: iv,
            mode: CryptJS.mode.CBC,
            padding: CryptJS.pad.Pkcs7
        })
        // console.log(encrypted)
        return CryptJS.enc.Base64.stringify(encrypted.ciphertext);
    },

    /**
     * AES 解密
     * @param：_content 待解密的内容[Base64处理过的]
     * @param：解密用的 AES key
     * @param: 初始化向量
     * @return 返回以 UTF-8 处理之后的明文
     */
    decrypt: function (_content, _key, _iv) {
        // let content = CryptJS.enc.Base64.parse(_content);
        // content = CryptJS.enc.Base64.stringify(content);
        let aesKey = CryptJS.enc.Utf8.parse(_key);
        let iv = CryptJS.enc.Utf8.parse(_iv);

        // 解密
        let decrypted = CryptJS.AES.decrypt(_content, aesKey, {
            iv: iv,
            mode: CryptJS.mode.CBC,
            padding: CryptJS.pad.Pkcs7
        })
        // console.log(decrypted)
        return decrypted.toString(CryptJS.enc.Utf8);
    }
}
export const DealwithExport = {
    excel: function(data,name = '') {
        const fileName = name + '.xls'
        let blob = new Blob([data], {type: "application/vnd.ms-excel"})
        let a = document.createElement('a')
        let url = window.URL.createObjectURL(blob)
        a.href = url
        a.download = fileName
        a.style.display = 'none'
        document.body.append(a)
        a.click()
        a.remove()
        window.URL.revokeObjectURL(url)
    }
}
