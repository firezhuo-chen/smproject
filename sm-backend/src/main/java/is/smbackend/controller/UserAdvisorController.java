package is.smbackend.controller;

import is.smbackend.pojo.UserAdvisor;
import is.smbackend.service.UserAdvisorService;
import is.smbackend.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/userAdvisor")
public class UserAdvisorController {

    @Autowired
    private UserAdvisorService userAdvisorService;

    @GetMapping
    public Result<List<UserAdvisor>> getAll() {
        return Result.success(userAdvisorService.list());
    }

    @GetMapping("/{id}")
    public Result<UserAdvisor> getById(@PathVariable String id) {
        return Result.success(userAdvisorService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody UserAdvisor userAdvisor) {
        return Result.success(userAdvisorService.save(userAdvisor));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody UserAdvisor userAdvisor) {
        return Result.success(userAdvisorService.updateById(userAdvisor));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(userAdvisorService.removeById(id));
    }
}
