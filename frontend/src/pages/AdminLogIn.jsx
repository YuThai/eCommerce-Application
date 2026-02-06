import React, { useState, useEffect } from "react";
import "../comp_css/Login.css";
import { useNavigate } from "react-router-dom";
import { loginUser } from "../Router/api";

const formData = {
  email: "",
  password: "",
};
const AdminLogin = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState(formData);

  useEffect(() => {
    document.title = "Ecommerse | Admin LogIn";
    return () => {
      document.title = "Ecommerse App";
    };
  }, []);
  const setHandlerChange = (e) => {
    const val = e.target.value;
    setForm({ ...form, [e.target.name]: val });
  };
  const submitHandler = async (e) => {
    e.preventDefault();

    try {
      const response = await loginUser(form.email, form.password);
      if (response && response.accessToken) {
        const role = response.role || response.userRole;
        if (role !== "ROLE_ADMIN" && role !== "ADMIN") {
          alert("Bạn không có quyền ADMIN");
          return;
        }
        localStorage.setItem("adminid", response.userId);
        alert("Admin Login successfully");
        navigate("/admin/admin");
      } else {
        alert("Invalid Credential");
        console.error("JWT retrieval failed");
      }
    } catch (error) {
      if (error && typeof error === "object" && error.message) {
        alert(error.message);
      } else {
        alert("Error during login. Please try again later.");
        console.error("Error during login:", error);
      }
    }
  };

  const { email, password } = form;

  return (
    <>
      <h2 style={{ textAlign: "center", color: "White", margin: "10px" }}>
        WELCOME TO ADMIN LOGIN PAGE
      </h2>

      <div className="loginConatiner">
        <div className="login-form">
          <h2 style={{ textAlign: "center" }}>Admin LogIn </h2>
          <form onSubmit={submitHandler}>
            <div className="form-group">
              <label htmlFor="email">Email:</label>
              <input
                id="email"
                type="text"
                name="email"
                value={email}
                onChange={setHandlerChange}
              />
            </div>
            <br />
            <div className="form-group">
              <label>Password:</label>
              <input
                type="password"
                name="password"
                value={password}
                onChange={setHandlerChange}
              />
            </div>
            <div className="form-group">
              <input type="submit" value="Login" />
            </div>
          </form>
        </div>
      </div>
    </>
  );
};

export default AdminLogin;
