package ncaaprs.ncaaprs_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PrsController {



	@GetMapping("/")
	public String index() {
		return "Welcome to NCAAPRS";
	}


}