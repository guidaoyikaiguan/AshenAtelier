// 邮箱校验 + 密码校验，和你的rules中调用一致
export default {
  // 邮箱正则校验
  email(rule, value, callback) {
    const reg = /^([a-zA-Z0-9_-])+@([a-zA-Z0-9_-])+(\.[a-zA-Z0-9_-])+/;
    if (!reg.test(value)) {
      callback(new Error('请输入正确的邮箱'));
    } else {
      callback();
    }
  },
  // 密码正则：8-18位 数字/字母/特殊字符
  password(rule, value, callback) {
    const reg = /^(?=.*[a-zA-Z0-9])(?=.*[^a-zA-Z0-9])[\S]{8,18}$/;
    if (!reg.test(value)) {
      callback(new Error('密码只能是数字，字母，特殊字符 8-18位'));
    } else {
      callback();
    }
  }
};