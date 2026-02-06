import React, { useState, useEffect } from "react";
import "../comp_css/Login.css";
import { useNavigate, Link } from "react-router-dom";
import { loginUser } from "../Router/api";
import loginbg from "../picture/loginbg1.webp";

// Using centralized API client for production deployment
const bg = {
  backgroundImage: `url(${loginbg})`,
  backgroundSize: "cover",
  backgroundRepeat: "no-repeat",
  backgroundPosition: "center center",
  border: "1px solid grey",
  height: "100vh",
};

const formData = {
  email: "",
  password: "",
};

const Login = () => {
  const navigate = useNavigate();
  const [form, setForm] = useState(formData);
  
  useEffect(() => {
    document.title = 'Ecommerse | LogIn';
    return () => { 
      document.title = 'Ecommerse App';
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
        localStorage.setItem("name", form.email || "LogIn");
        alert("Login successfully");
        navigate("/");
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
    <div style={bg}>
      <h2 style={{ textAlign: "center", color: "White", margin: "20px" }}>
       WELCOME TO USER LOGIN PAGE
      </h2>
      <div className="loginConatiner" >
        <div className="login-form">
          <h2 style={{ textAlign: "center" }}>LogIn </h2>
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
              <p>
                Don't have an account?{" "}
                <Link to="/register-user">Register here</Link>
              </p>
            </div>
          </form>
        </div>
      </div>
      </div>
    </>
  );
};

export default Login;
