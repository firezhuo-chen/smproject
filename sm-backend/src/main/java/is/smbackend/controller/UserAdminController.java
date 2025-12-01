package is.smbackend.controller;

import is.smbackend.pojo.UserAdmin;
import is.smbackend.service.UserAdminService;
import is.smbackend.response.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/userAdmin")
public class UserAdminController {

    @Autowired
    private UserAdminService userAdminService;

    @GetMapping
    public Result<List<UserAdmin>> getAll() {
        return Result.success(userAdminService.list());
    }

    @GetMapping("/{id}")
    public Result<UserAdmin> getById(@PathVariable String id) {
        return Result.success(userAdminService.getById(id));
    }

    @PostMapping
    public Result<Boolean> save(@RequestBody UserAdmin userAdmin) {
        return Result.success(userAdminService.save(userAdmin));
    }

    @PutMapping
    public Result<Boolean> update(@RequestBody UserAdmin userAdmin) {
        return Result.success(userAdminService.updateById(userAdmin));
    }

    @DeleteMapping("/{id}")
    public Result<Boolean> remove(@PathVariable String id) {
        return Result.success(userAdminService.removeById(id));
    }
}
