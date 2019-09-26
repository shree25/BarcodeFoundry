package com.generator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Random;

@Controller
@RequestMapping(path="/")
public class HomeController {

	@Autowired
	private UserRepository userRepository;

	protected String getSaltString() {
		String SALTCHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
		StringBuilder salt = new StringBuilder();
		Random rnd = new Random();
		while (salt.length() < 30) { // length of the random string.
			int index = (int) (rnd.nextFloat() * SALTCHARS.length());
			salt.append(SALTCHARS.charAt(index));
		}
		String saltStr = salt.toString();
		return saltStr;

	}

	@RequestMapping(path="/loaderio-d34bb9aa42c09d9ef54be49f42d2f238")
	public String loaderIoVerification() {
		return "loaderio";
	}

	@RequestMapping(path="/")
	public String defaultPage() {
		return "redirect:login";
	}

	@RequestMapping(path="/login")
	public String login(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null)
		{
			return "redirect:welcome";
		}
		return "login";
	}

	@RequestMapping(path = "/loginUser", method = RequestMethod.POST)
	public String login(
			@RequestParam("email") String email,
			@RequestParam("password") String password,
			HttpSession session,
			HttpServletResponse httpResponse) {

		if (session.getAttribute("loggedInUser") != null)
		{
			return "redirect:welcome";
		}

		Boolean loginStatus = this.loginCheck(email, password, session);

		if(loginStatus == true) {
			return "redirect:welcome";
		} else {
			return "redirect:login";
		}
	}

	private Boolean loginCheck(@RequestParam("email") String email,
							   @RequestParam("password") String password,
							   HttpSession session)
	{
		User user = userRepository.findByEmail(email);

		if (user != null)
		{
			String passwordFromDb = user.getPassword()+"";

			if (passwordFromDb.equals(password))
			{
				session.setAttribute("loggedInUser", email);
				return true;
			}else{
				return false;
			}
		}else {
			return false;
		}
	}

	@RequestMapping(value = "/logout", method = RequestMethod.GET)
	public String logout(HttpSession session) {
		session.removeAttribute("loggedInUser");
		return "redirect:login#logout";
	}

	@RequestMapping("/register")
	public String register(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null)
		{
			return "redirect:welcome";
		}
		return "register";
	}

	@PostMapping(path="/registerUser") // Map ONLY POST Requests
	public @ResponseBody String addNewUser (@RequestParam String name,
											@RequestParam String email,
											@RequestParam String password,
											HttpSession session) {

		if (session.getAttribute("loggedInUser") != null)
		{
			return "redirect:welcome";
		}

		User n = new User();
		n.setName(name);
		n.setEmail(email);
		n.setPassword(password);
		n.setToken(this.getSaltString());
		userRepository.save(n);
		return "Saved";
	}

	@RequestMapping("/welcome")
	public String welcome(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null)
		{
			return "welcome";
		}
		return "redirect:login";
	}

	@RequestMapping("/codetoimage")
	public String converter(HttpSession session) {
		if (session.getAttribute("loggedInUser") != null)
		{
			return "converttoimage";
		}
		return "redirect:login";
	}

}