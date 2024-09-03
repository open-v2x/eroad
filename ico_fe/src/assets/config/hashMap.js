function HashMap() {
	//特殊关键字(specialKey)用来处理特殊的保留字
	//这些保留字主要是Object对象中的固有属性和方法
	var specialKey = [
		'valueOf',
		'constructor',
		'toString',
		'toLocaleString',
		'ropertyIsEnumerable',
		'hasOwnProperty',
		'isprototypeOf',
		'prototype'
	];
	//为特殊关键字提供单独存放值的空间
	var speicalValue = new Array(specialKey.length);

	//特殊关键字存取标记,true 表示该位已经有值
	var speicalFlag = new Array(specialKey.length);

	//存放普通的关键字，只需要一个普通的Object
	var normalHashMap = {};

	//将数值插入到HashMap
	this.put = function (key, value) {
		if (!key) {
			return false;
		}
		//处理特殊关键字
		for (var i = 0; i < specialKey.length; i++) {
			if (key == specialKey[i]) {//如果关键字等于某个特殊关键字
				speicalValue[i] = value;
				speicalFlag[i] = true;
				return true;
			}
		}
		//处理普通关键字
		normalHashMap[key] = value;
		return true;
	}

	this.get = function (key) {
		if (!key) {
			return null;
		}
		//先处理特殊关键字
		for (var i = 0; i < specialKey.length; i++) {
			if (key == specialKey[i]) {
				if (speicalValue[i]) {
					return speicalValue[i];
				} else {
					return null;
				}
			}
		}
		if (key in normalHashMap) {
			return normalHashMap[key];
		} else {
			return null;
		}
	}

	//清空HashMap
	this.clear = function () {
		speicalValue = new Array(specialKey.length);
		speicalFlag = new Array(specialKey.length);
		normalHashMap = {};
	}

	//判断是否包含key
	this.containsKey = function (key) {
		//先处理特殊关键字
		for (var i = 0; i < specialKey.length; i++) {
			if (key == specialKey[i]) {
				if (speicalFlag[i] == true) {//和关键字相同并且关键字的标志位为true
					return true;
				} else {
					return false;
				}
			}
		}
		//处理普通关键字
		if (key in normalHashMap) {
			return true;
		} else {
			return false;
		}
	}

	//判断是否包含Value
	this.containsValue = function (value) {
		if (!value) {
			return false;
		}
		var contains = false;
		for (var i = 0; i < speicalValue.length; i++) {
			var v = speicalValue[i];
			if (v == value) {
				contains = true;
				break;
			}
		}
		if (contains) {
			return contains;
		}
		for (var key in normalHashMap) {
			var v = normalHashMap[key];
			if (v == value) {
				contains = true
				break;
			}
		}
		return contains;
	}

	//返回hashMap的长度
	this.size = function () {
		var size = 0;
		for (var i = 0; i < speicalValue.length; i++) {
			if (speicalFlag[i] == true) {
				size++;
			}
		}
		for (var key in normalHashMap) {
			size++;
		}
		return size;
	}

	//判断hashMap是否为空
	this.isEmpty = function () {
		return this.size() == 0 ? true : false;
	}


	//根据一个key删除元素
	this.remove = function (key) {
		if (!key) {
			return false;
		}
		for (var i = 0; i < specialKey.length; i++) {
			if (key == specialKey[i] && speicalFlag[i] == true) {
				speicalFlag[i] = null;
				speicalValue[i] = null;
				return true;
			} else if (key == specialKey[i] && speicalFlag[i] == false) {
				return false;
			}
		}
		for (var k in normalHashMap) {
			if (k == key) {
				delete normalHashMap[k];
				return true;
			}
		}
		return false;
	}

	//取得Key值
	this.keySet = function () {
		return getStr(0);
	}

	//toString 方法
	//{phoneNo=13800138000, userName=tom}
	this.toString = function () {
		return getStr(1);
	}

	//返回类似java中Map的entrySet
	//[phoneNo=13800138000, userName=tom]
	this.entrySet = function () {
		return getStr(2);
	};

	//取得所有的values
	this.values = function () {
		return getStr(3);
	}

	//公共方法，用于封装返回字符串
	var getStr = function (flag) {
		//0： 中括号 返回keys
		//1:  花括号 返回keys和values组合
		//2： 中括号 返回keys和values组合
		//3： 中括号 返回values
		var result = "";
		for (var i = 0; i < specialKey.length; i++) {
			if (speicalValue[i] && speicalFlag[i] == true) {
				if (flag == 0) {
					result += specialKey[i] + ",";
				} else if (flag == 3) {
					result += JSON.stringify(speicalValue[i]) + ",";
				} else if (flag == 2) {
					result += specialKey[i] + "=" + JSON.stringify(speicalValue[i]) + ",";
				} else {
					result += specialKey[i] + "=" + JSON.stringify(speicalValue[i]) + "," + " ";
				}
			}
		}
		for (var key in normalHashMap) {
			if (flag == 0) {
				result += key + ",";
			} else if (flag == 3) {
				result += JSON.stringify(normalHashMap[key]) + ",";
			} else if (flag == 2) {
				result += key + "=" + JSON.stringify(normalHashMap[key]) + ",";
			} else {
				result += key + "=" + JSON.stringify(normalHashMap[key]) + "," + " ";
			}
		}
		if (flag == 1) {
			result = result.substring(0, result.length - 2)
			result = "{" + result + "}";
		} else {
			result = result.substring(0, result.length - 1);
			result = "[" + result + "]";
		}
		return result;
	}
}

export default HashMap

