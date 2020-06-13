<%--
 Created by IntelliJ IDEA.
 User: steadyjack
 Date: 2019/5/20
 Time: 11:59
 To change this template use File | Settings | File Templates.
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@include file="tag.jsp" %>
<c:set var="ctx" value="${pageContext.request.contextPath}"/>
<html>
<head>
    <title>商城高并发抢购-用户注册</title>
    <%@include file="head.jsp" %>
</head>
<body>
<div class="container">
    <div class="panel panel-default">
        <div class="panel-heading">
            <h1 align="center">这是用户注册页</h1>

            <form action="${ctx}/signup" method="post" >
                <table align="center" border="0">
                    <tr>
                        <td>用户名:</td>
                        <td><input id="userName" type="text" name="userName" value="${userName}"></td>
                    </tr>
                    <br/>
                    <tr>
                        <td>密&nbsp;&nbsp;码:</td>
                        <td><input id="password" type="password" name="password"></td>
                    </tr>
                    <br/>
                    <tr>
                        <td>手&nbsp;&nbsp;机:</td>
                        <td><input type="phone" name="phone"></td>
                    </tr>
                    <br/>
                    <tr>
                        <td>邮&nbsp;&nbsp;箱:</td>
                        <td><input type="email" name="email"></td>
                    </tr>
                    <tr>
                        <td><input type="submit" value="提交" name="signup" onclick="return register()"></td>
                    </tr>
                </table>
            </form>
            <h2 id="msg" align="center">${msg}</h2>
        </div>
    </div>
</div>
</body>
<script src="${ctx}/static/plugins/jquery.js"></script>
<script src="${ctx}/static/plugins/bootstrap-3.3.0/js/bootstrap.min.js"></script>
<script src="${ctx}/static/plugins/jquery.cookie.min.js"></script>
<script src="${ctx}/static/plugins/jquery.countdown.min.js"></script>
<script>
    function register(){
        //得到name输入框对象
        var name = document.getElementById("userName");
        //判断输入框是否有内容
        if(name.value.length==0){
            confirm("用户名不能为空");
            return false;
        }
        var pass = document.getElementById("password");
        if(pass.value.length==0){
            confirm("密码不能为空");
            return false;
        }
        return true;
    }
</script>
</html>
















