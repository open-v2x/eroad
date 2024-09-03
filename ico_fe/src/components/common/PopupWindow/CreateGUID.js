/**
 * 生成guid
 */
export default () => {
    var CHARS = '0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz'.split('')
    var chars = CHARS,
        uuid = [],
        i
    // rfc4122, version 4 form
    var r
    // rfc4122 requires these characters
    uuid[8] = uuid[13] = uuid[18] = uuid[23] = '-'
    uuid[14] = '4'
    for (i = 0; i < 36; i++) {
        if (!uuid[i]) {
            r = 0 | (Math.random() * 16)
            uuid[i] = chars[i == 19 ? (r & 0x3) | 0x8 : r]
        }
    }

    var ret = uuid.join('')
    return ret;
}