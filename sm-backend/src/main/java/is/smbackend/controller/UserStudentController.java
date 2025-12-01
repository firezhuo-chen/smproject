package is.smbackend.controller;

import is.smbackend.pojo.UserStudent;
import is.smbackend.service.UserStudentService;
import is.smbackend.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/userStudent")
public class UserStudentController {

    @Autowired
    private UserStudentService userStudentService;

    @GetMapping
    public Result<List<UserStudent>> getAll() {
        return Result.success(userStudentService.list());
    }

    @GetMapping("/{id}")
    public Result<UserStudent> getById(@PathVariable String id) {
        return Result.success(userStudentService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody UserStudent userStudent) {
        return Result.success(userStudentService.save(userStudent));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody UserStudent userStudent) {
        return Result.success(userStudentService.updateById(userStudent));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(userStudentService.removeById(id));
    }
}
